# Security notes - KBC

## Region guard (v1.1.23)

The app refuses to open when the phone's SIM or network country is on a built-in block list (PK, BD, AF, CN, KP). The check runs only on the phone, uses no permission, no network call and no IP lookup, and nothing is stored or sent. With no signal, or an Indian SIM, it never blocks. It is a deterrent, not foolproof: removing the SIM or changing the language region bypasses it.

## Log privacy (v1.1.20)

Log lines no longer include personal content: the Hub no longer writes spoken text to the device log, and KBC no longer writes the player's name. Nothing else changes. No new permission, library or server.

## Report delivery fix (v1.1.19)

Crash and feedback reports from the app now send the website address as the origin header, so the report service accepts them. Reports that could not be delivered before (they stay saved on the phone and retry at the next start) will now go through. Report contents, the stored file and the opt-in text are unchanged. No new permission, library or server.

## Age-based question level (v1.1.16)

Junior players (Student mode, or a saved age of 5 to 17) now get questions only from the class pool that matches their class, or the class for their age when no class was chosen. Adult pools are no longer mixed into junior games. Adults keep their own pools. This uses only the age and class already saved on the device. No new permission, library or network call.

## Installer file cleanup (v1.1.15)

When the app comes back to the front it also deletes installer files older than one hour from its private cache folder. Nothing outside the app's own folder is touched. No new permission, library or network call.

## Update download progress (v1.1.14)

The in-app update now shows real progress: percent downloaded counting up, megabytes done of total, a bar, and a time left that never goes up. After the download the installer file can be installed again from the card if the installer was closed, and it is deleted after install or at the next start. Only the app's private cache folder is used. No new permission, library or network call.

## Update alert (v1.1.13)

A background check every 6 hours reads the same public latest.json the in-app updater already uses and, when a newer version exists, shows one notification. Tapping it runs the existing verified download (size and SHA-256 checked) and opens the Android installer. Adds the AndroidX WorkManager library (2.10.0) where it was not already present. No new permission, server or stored personal data.

## Anti-cheat removed completely (v1.1.12)
- The game no longer records or listens through the microphone, no longer shows a camera box, and no longer warns or disqualifies a player. The noise check, the 3-strike disqualification, the spoken warnings, the "Anti-cheat is off" line and the "disqualified" result screen are all gone from the code.
- The app does not ask for Camera or Microphone now. The Profile screen has no Grant buttons for them. Camera will be asked only when the QR scan that links KBC with KBC Web ships. Microphone will be asked only when voice answers ship.
- The two permissions stay declared in the app, with no new permission, library or network call. Settings > Permissions explains each one in plain words.

## Plain failure messages (v1.1.11)
- When an update download fails, the app now shows one plain sentence (for example "No internet, or the server did not answer") instead of the raw system text such as "Unable to resolve host". The app's own messages (checksum, allow installs) are unchanged.
- No new permission, library or network call.

## Denied permissions no longer block the game (v1.1.10)
- Before, a game would not start if Camera, Microphone or Notifications was missing. Now the game always starts. If Camera or Microphone is not allowed, only the anti-cheat part that needs it is off, and the game screen shows one line saying which permission is missing. A missing Notifications permission never blocks anything; it only means no reminders.
- No new permission, library or network call. The Profile screen and the Permissions list still show each permission and its reason.

## UI cleanup, step 1 (v1.1.9)
- The message shown when the saved questions are stale no longer mentions a "watchdog" or "game state error". It now says the questions could not be used and to start a new game. The rule behind it is unchanged.
- No new permission, library or network call.

## Standard header (new in 1.1.1)
The header is the Netra standard: 56 dp, fixed, only the app name, the installed version (read from the package, "Unavailable" if it cannot be read) and the device date and time. Everything else scrolls. No new permission, network call or library.

## UPI ID removed (new in 1.1.0)
- Before 1.1.0 the profile screen asked for an optional UPI ID "to receive prizes", and after a game the app logged that it was "securely transmitting" a payout record. Nothing was ever sent anywhere and the app pays no prizes: it only counts points. Showing that field and that message was not true, so both are removed. The app no longer asks for, keeps or uses a UPI ID. Saving the profile clears any UPI ID that an older version stored on this phone. No money, payment or payout feature exists in KBC. Permissions: none added or removed. No new library.
- Rule for all Netra apps: every datum shown must be backed by real evidence; when none is available the app shows "Unavailable" and nothing is made up.

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
- Version 1.1.2: Settings has a manual "Send crash report" button. It shows the exact text first (app, app version, phone model, Android version, the last crash trace) and sends only if the user taps Send; "Share instead" lets the user pick any app. If no crash is saved it says Unavailable. The automatic send now counts as sent only when the forwarding service confirms; before, an HTTP 200 reply was enough, so a report could be deleted without any email being sent.
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

## Installer file cleanup
After an in-app update installs, the app restarts and, on start, deletes every downloaded installer file from its cache folder (`cache/updates/`). Nothing from the update is left in storage. A new download also removes older files first. If the user cancels the install, the file is removed the next time the app starts.

## Manual update check
- Settings has a "Check for update" button. It reads the same latest.json as the automatic check, shows "You are on the latest version", "Update available: vX" or "Unavailable: could not check", and never installs anything without the user tapping Update and confirming in the Android installer. The file is still checked for size and SHA-256 first.

## KBC Web link (version 1.1.3)

- New Profile card "KBC Web" with a button that opens https://prayagi-store-and-services.github.io/KBC/play.html in the phone's browser. The app itself sends nothing and reads nothing for this; it only asks Android to open the address. If no browser exists it shows Unavailable.
- No new permission, network call from the app, or library.

## Home screen widget (version 1.1.4)

- New widget "KBC - TarkShastra": shows the points of your last finished game, your best score and how that game ended, from what the app saved when the game ended. Before any game is finished it shows "Unavailable". The score is in points, as in the game.
- Stored on this phone only (local preferences): the last game's points, question reached, correct answers, end reason, time, and the best score. Nothing is sent anywhere. Note: KBC still has Android app backup on with sample rules (an owner question is open); this small file follows that setting.
- No timer or background work: the app redraws the widget when a game ends.
- No new permission, network call or library. The widget receiver is exported because Android's launcher must send it update events; it handles only that action. Tapping the widget opens the app.

## Permissions list (version 1.1.5)

- New "Permissions" card in Profile: lists each permission the app uses (Camera, Microphone, Notifications, Install apps, Internet), the plain reason, and the live status read from Android when you open the screen (no timer). Tapping a row opens the Android page for this app where you can allow or stop it. The older Grant buttons stay.
- No new permission, network call or library.

## Truthful profile-setup screen (version 1.1.7)

The screen shown after saving the profile said "100% offline game bank ready" and "offline bank installation" although that step only saves the profile and syncs current affairs; no question bank is downloaded or checked there. The texts now say profile setup and starting the game. Text change only: no new permission, network call, library or timer.

## Festival banner (version 1.1.8)

- A card near the top of the home screen shows today's festival (India calendar, bundled in the app, from timeanddate.com India 2026-2027) or "coming soon" for a festival within 3 days, with the live date and time. India's Independence Day (15 August) is shown too. It has no death anniversaries and no other country's days. After 2027 there is no data, so no banner is shown and nothing is invented. A date marked "may differ by a day" says so.
- It works offline. No new permission, network call or library.
