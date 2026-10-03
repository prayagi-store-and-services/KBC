#!/usr/bin/env python3
"""KBC question pool generator (Netra). Standard library only.

Generates quiz questions with Gemini, checks each one with a second independent call,
and keeps only questions where both calls agree on the correct option.
The API key comes from the GEMINI_API_KEY environment variable (GitHub secret) and is never written to disk.
"""
import json, os, re, sys, time, hashlib, datetime, urllib.request, urllib.error

KEY = os.environ.get("GEMINI_API_KEY", "").strip()
BASE = "https://generativelanguage.googleapis.com/v1beta"
OUT = os.environ.get("POOL_DIR", "pool")
MAX_CALLS = int(os.environ.get("MAX_CALLS", "40"))      # hard cap per run, protects the free tier
PER_BATCH = int(os.environ.get("PER_BATCH", "8"))
TARGET_NEW = int(os.environ.get("TARGET_NEW", "100"))
calls = 0

# group -> description used in the prompt. Tier bands map to the 17 game levels.
GROUPS = {
    "class-5": "Class 5 school student in India (NCERT level)",
    "class-8": "Class 8 school student in India (NCERT level)",
    "class-10": "Class 10 school student in India (NCERT level)",
    "class-12": "Class 12 school student in India (NCERT level)",
    "upsc": "UPSC / Civil Services aspirant (India)",
    "ssc": "SSC / State exam aspirant (India)",
    "banking": "Banking / IBPS aspirant (India)",
    "general": "general adult player in India",
}
BANDS = {"easy": (1, 5), "medium": (6, 11), "hard": (12, 17)}

SPECIAL_DAYS = {  # (month, day): topic. Fixed official dates only.
    (10, 2): "Gandhi Jayanti (Mahatma Gandhi)",
    (8, 15): "Independence Day of India",
    (1, 26): "Republic Day of India",
    (11, 14): "Children's Day (Jawaharlal Nehru)",
    (9, 5): "Teachers' Day (Dr. S. Radhakrishnan)",
    (10, 31): "National Unity Day (Sardar Vallabhbhai Patel)",
    (4, 14): "Ambedkar Jayanti (Dr. B. R. Ambedkar)",
    (6, 5): "World Environment Day",
    (10, 15): "World Students' Day / Dr. A. P. J. Abdul Kalam",
    (12, 25): "Christmas (Good Governance Day in India)",
}

def api(path, body=None):
    global calls
    if calls >= MAX_CALLS:
        raise RuntimeError("call budget reached")
    calls += 1
    url = f"{BASE}/{path}"
    req = urllib.request.Request(url, data=None if body is None else json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json", "x-goog-api-key": KEY})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=90) as r:
                return json.loads(r.read())
        except urllib.error.HTTPError as e:
            if e.code in (429, 500, 503) and attempt < 2:
                time.sleep(20 * (attempt + 1)); continue
            raise RuntimeError(f"HTTP {e.code} from Gemini (quota or service)")
    raise RuntimeError("Gemini unavailable")

def pick_model():
    forced = os.environ.get("GEMINI_MODEL", "").strip()
    if forced:
        return forced
    data = api("models?pageSize=200")
    names = [m["name"].split("/")[-1] for m in data.get("models", [])
             if "generateContent" in m.get("supportedGenerationMethods", [])]
    def score(n):
        nums = re.findall(r"\d+(?:\.\d+)?", n)
        v = float(nums[0]) if nums else 0
        return (("flash-lite" in n), v)
    cands = [n for n in names if "flash-lite" in n and "preview" not in n and "image" not in n and "tts" not in n]
    cands = cands or [n for n in names if "flash" in n and "image" not in n and "tts" not in n]
    if not cands:
        raise RuntimeError("no usable Gemini model found")
    return sorted(cands, key=score, reverse=True)[0]

def ask(model, prompt, schema=None, temp=0.7):
    body = {"contents": [{"parts": [{"text": prompt}]}],
            "generationConfig": {"temperature": temp, "responseMimeType": "application/json"}}
    d = api(f"models/{model}:generateContent", body)
    try:
        txt = d["candidates"][0]["content"]["parts"][0]["text"]
        return json.loads(txt)
    except Exception:
        return None

def norm(t):
    return re.sub(r"[^a-z0-9\u0900-\u097f]+", " ", t.lower()).strip()

def gen_prompt(group, band, n, special):
    sp = f"\nEvery question must be about: {special}." if special else ""
    return f"""Write {n} multiple-choice quiz questions for a {GROUPS[group]}. Difficulty band: {band}.{sp}
Rules: factual, verifiable, timeless or clearly dated, one unambiguously correct option, no opinion, no politics-of-the-day,
nothing about Pakistan, nothing about death anniversaries, no trick questions, and avoid contested superlatives (e.g. longest river, highest waterfall, first in space) where sources disagree on the measurement or definition. Exactly 4 options. Do not repeat a topic inside the list.
Options in "optsEn" and the text in "qEn" must be ENGLISH ONLY (no Hindi, no slashes); Hindi goes only in "qHi" and "optsHi". Return a JSON array. Each item: {{"qEn": str, "qHi": str (Hindi), "optsEn": [4 str], "optsHi": [4 str], "correct": 0-3,
"explainEn": short reason, "sourceHint": a well-known public source such as NCERT chapter, Constitution article, PIB, ISRO, Britannica}}."""

def verify_prompt(qs):
    blocks = []
    for i, q in enumerate(qs):
        opts = "\n".join(f"  {j}: {o}" for j, o in enumerate(q["optsEn"]))
        blocks.append(f"[{i}] {q['qEn']}\n{opts}")
    return ("You are a strict fact checker. For each quiz question below, answer using only well-established facts, "
            "without assuming any answer key. Return a JSON array with one object per question, in order: "
            '{"i": index, "answer": 0-3 or -1 if no option or more than one option is correct, "confidence": "high"|"low"}.\n\n'
            + "\n\n".join(blocks))

def valid_shape(q):
    try:
        return (isinstance(q["qEn"], str) and isinstance(q["qHi"], str) and len(q["optsEn"]) == 4 and len(q["optsHi"]) == 4
                and len(set(map(norm, q["optsEn"]))) == 4 and q["correct"] in (0, 1, 2, 3) and len(q["qEn"]) > 12)
    except Exception:
        return False

def banned(q):
    t = norm(" ".join([q["qEn"]] + q["optsEn"]))
    return "pakistan" in t or "death anniversary" in t or "punyatithi" in t

def load_pool():
    pool = {}
    for g in GROUPS:
        p = os.path.join(OUT, f"{g}.json")
        items = json.load(open(p)) if os.path.exists(p) else []
        # drop early entries whose English options carried Hindi text (older prompt)
        pool[g] = [q for q in items if not re.search(r"[\u0900-\u097f]", " ".join(q.get("optsEn", [])))]
    return pool

def played_ids():
    """Optional: read the played-question registry if its Firestore rule exists. Missing = nothing retired yet."""
    pid = os.environ.get("FIREBASE_PROJECT", "netra-ai-jan")
    url = f"https://firestore.googleapis.com/v1/projects/{pid}/databases/(default)/documents/netra_kbc_played?pageSize=1000"
    try:
        with urllib.request.urlopen(url, timeout=20) as r:
            d = json.loads(r.read())
        return {x["name"].split("/")[-1] for x in d.get("documents", [])}
    except Exception:
        return set()

def main():
    if not KEY:
        print("GEMINI_API_KEY is not set"); return 1
    os.makedirs(OUT, exist_ok=True)
    model = pick_model()
    print("model:", model)
    pool = load_pool()
    played = played_ids()
    seen = {norm(q["qEn"]) for g in pool.values() for q in g}
    today = datetime.date.today()
    special = SPECIAL_DAYS.get((today.month, today.day))
    stats = {"asked": 0, "kept": 0, "rejected": 0}
    groups = list(GROUPS)
    off = int(time.time() // 21600) % len(groups)
    groups = groups[off:] + groups[:off]
    plan = []
    for i in range(1000):
        plan.append((groups[i % len(groups)], list(BANDS)[(i // len(groups)) % 3], special if (special and i % 4 == 0) else None))
    new = 0
    try:
        for group, band, sp in plan:
            if new >= TARGET_NEW:
                break
            items = ask(model, gen_prompt(group, band, PER_BATCH, sp))
            if not isinstance(items, list):
                continue
            cands = []
            for q in items:
                stats["asked"] += 1
                if not valid_shape(q) or banned(q) or norm(q["qEn"]) in seen:
                    stats["rejected"] += 1; continue
                cands.append(q)
            if not cands:
                continue
            v = ask(model, verify_prompt(cands), temp=0.0)
            votes = {x.get("i"): x for x in v if isinstance(x, dict)} if isinstance(v, list) else {}
            for idx, q in enumerate(cands):
                x = votes.get(idx)
                if not (x and x.get("answer") == q["correct"] and x.get("confidence") == "high"):
                    stats["rejected"] += 1; continue
                lo, hi = BANDS[band]
                qid = "q_" + hashlib.sha256(norm(q["qEn"]).encode()).hexdigest()[:16]
                if qid in played:
                    continue
                pool[group].append({
                    "id": qid, "group": group, "band": band, "tierFrom": lo, "tierTo": hi,
                    "special": sp, "qEn": q["qEn"], "qHi": q["qHi"], "optsEn": q["optsEn"], "optsHi": q["optsHi"],
                    "correct": q["correct"], "explainEn": q.get("explainEn", ""), "sourceHint": q.get("sourceHint", ""),
                    "verifiedBy": "AI second pass agreed (high confidence)", "model": model,
                    "createdAt": datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")})
                seen.add(norm(q["qEn"])); new += 1; stats["kept"] += 1
    except RuntimeError as e:
        print("stopped early:", e)
    for g, items in pool.items():
        json.dump(items, open(os.path.join(OUT, f"{g}.json"), "w"), ensure_ascii=False, indent=1)
    index = {"updatedAt": datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ"),
             "counts": {g: len(v) for g, v in pool.items()}, "model": model,
             "note": "AI generated, each question double checked by a second independent call; can still contain errors."}
    json.dump(index, open(os.path.join(OUT, "index.json"), "w"), indent=1)
    print(json.dumps({"calls": calls, **stats, "counts": index["counts"]}))
    return 0

if __name__ == "__main__":
    sys.exit(main())
