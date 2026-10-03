# Security notes - KBC

## In-app update (new)

What it does: on app open, at most once a day, the app asks `https://github.com/prayagi-store-and-services/KBC/releases/latest/download/latest.json` whether a newer version exists. If yes, it shows the version and what changed, and the user taps Update. The app downloads `app-release.apk` from the same release, checks its size and SHA-256 against latest.json, and only then opens the Android package installer. The user confirms with one system tap.

What is protected:
- Only https://github.com/prayagi-store-and-services/KBC/ release URLs are used; the download URL is built from the release tag, never taken from the metadata.
- The file is deleted and not installed if its size or SHA-256 does not match.
- Android installs the update only if it is signed with the same key as the installed app (the Netra release key), so a different signer is rejected by the system.
- Nothing about the user or device is sent: the check is a plain download of a small public file. No account, no ID, no location.
- Permission added: REQUEST_INSTALL_PACKAGES (needed to open the installer; the user must also allow installs from this app once in Android settings). A separate FileProvider (`<applicationId>.updates`) exposes only the app cache folder `updates/`.

Release process: the Signed Release workflow publishes `app-release.apk`, a named copy, and `latest.json` together. The tag must equal `v` plus the versionName in app/build.gradle.kts.

Limits: Android does not allow silent installs, so the user always taps once. Versions installed before this feature existed cannot update themselves and must be installed manually once.

## Anonymous usage count (new in 1.0.2)

Once per UTC day (and once per month) the app adds 1 to a public counter in Firestore (`netra_active/kbc_<yyyyMMdd>` and `_<yyyyMM>`), so the Netra Eco website can show approximate active users. The request contains only the counter document name and "increment by 1". No device ID, install ID, account, location or app data is sent, and the app keeps no ID for this. A local flag stops repeats on the same day; a failed send is retried at the next open. It is on by default and can be turned off with the "Share anonymous usage count" switch. Firestore rules allow only creating a counter with value 1 or raising it by exactly 1; counters are public to read. Anyone could in theory script extra +1s, so the number is approximate, and reinstalling or clearing data can count one person twice.

## Automatic crash reports
- If the app crashes, it saves a short report on the device. The next time the app opens, it sends that report by itself (no button, no question) and then deletes it. If the send fails, it is kept and retried at the next start.
- The report contains only: the app name, phone model, Android version, app version, and the crash stack trace (exception class names and code locations; exception messages are dropped on purpose).
- It contains no name, email, location, files, contacts, device IDs or usage history.
- It is sent through the same form pipeline as the website forms (FormSubmit) to the developer's email.

## Question generator (new, server side only)
- A scheduled GitHub Actions job (`.github/workflows/kbc-questions.yml`, script `tools/kbc_gen/generate.py`) writes quiz questions with Google Gemini. Each question is checked by a second, independent Gemini call and kept only if both agree on the correct option with high confidence. It can still be wrong; a wrong question can be reported and removed.
- The Gemini API key is a GitHub Actions secret (`GEMINI_API_KEY`). It is never in the app, the repo, a site or any log. The app never talks to Gemini.
- Output goes to a data-only branch `question-pool` (public JSON, no personal data). `main` is not touched by the job, so branch protection is unchanged.
- The job sends Gemini only the prompt (class/group, difficulty, a special-day topic). No user data exists on the server side.
- Cost guard: at most 40 Gemini calls per run, every 6 hours, within the free tier. The job stops when quota is hit.

## Fresh question packs (new in 1.0.5)
- At game start the app downloads public JSON question files from `https://raw.githubusercontent.com/prayagi-store-and-services/KBC/question-pool/pool/` (one file for the player's class or exam group, plus a general file). The request carries no ID, account or location. It uses the existing OkHttp library, no new dependency.
- Question IDs already played are remembered on the device only (app preferences). Nothing about them is sent anywhere yet. Uploading played IDs for the website archive needs a Firestore rule that the owner has not published yet, so it is not in this version.
- The app never calls Gemini and holds no AI key. The questions are AI generated and double checked by the generator; they can still be wrong.
- If no fresh question can be found, this version still falls back to the built-in puzzles (a switch, `REQUIRE_FRESH_PACK`, can make the game refuse instead; pending owner decision).
