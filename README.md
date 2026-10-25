# Tanglish Voice AI — starter APK

**Status:** Starter source project. Not an already built APK. Build it using the provided GitHub Action. Actual installation and recording behavior has not been tested on your phone.

## What it does
- Creates a draggable floating microphone that can appear above WhatsApp, Instagram, etc.
- Tap it to dictate; recognises Tamil (TA) or English (EN) via the phone's installed speech recognition service.
- Transliterates Tamil script into *approximate* Tanglish using a simple offline rule-based converter and some common-word mappings.
- Copies output into the clipboard so you can **paste manually** into chats.
- Foreground notification, with Stop button inside the app.

**Limitations**
- The text converter is not a fluent AI translator. Spellings of less-common words can be wrong.
- Speech recognition may require a supported service, internet, and/or downloaded language packs.
- You must grant Microphone and Display Over Other Apps permissions. A visible foreground notification may appear. Behavior varies by Android brand/version.
- For security and privacy, **this version does NOT automatically insert into other apps, read chats, or send messages**.
- Google speech services may process speech according to your phone's speech provider settings.

## Easiest online APK build (no local Android SDK)
1. Unzip this project on your computer.
2. Open https://github.com/new and create a new **private** repository named `TanglishVoiceAI` (leave it empty: do not tick Initialize README).
3. On the new repo page select **uploading an existing file** or **Add file → Upload files**.
4. Drag the unzipped project's **contents** (including the `.github` folder) into the browser. You should have `app/`, `.github/workflows/build-apk.yml`, `build.gradle`, `settings.gradle`, `gradle.properties`, and `README.md` at the repo root. Note: you must upload the extracted files, not the ZIP itself.
5. Commit/upload the files to branch `main`.
6. Open the repo's **Actions** tab → **Build Tanglish Voice AI APK**. It should start on upload, or click **Run workflow**.
7. When the build succeeds, open the run → scroll to **Artifacts** → download `TanglishVoiceAI-debug-APK`.
8. Extract the downloaded ZIP to get `app-debug.apk`. Send it to your Android phone, open it, and accept the Android prompt to install from that source.
9. Open Tanglish Voice AI → Start → allow microphone and 'Display over other apps'. Tap Start again after granting permissions.

### Build errors?
Open GitHub Actions → latest run → step **Compile debug APK** and copy the error text. The online build will be dependent on GitHub and Google's Android build servers being available.

### Changing the app later
Edit the Java files in `app/src/main/java/com/tanglishvoiceai/app/` and push changes to `main`; GitHub Actions automatically creates a new debug APK.
