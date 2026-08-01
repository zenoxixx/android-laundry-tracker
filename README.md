# Laundry Tracker (Android, Java)

Offline clothes-laundry tracker. Room + Material 3 + MVVM.

## Run

1. Open this folder in Android Studio Ladybug (or newer).
2. Let Gradle sync (needs internet once to fetch dependencies).
3. Run on a device or emulator with **Android 8.0 (API 26)** or higher.

## Notes

- All data is stored locally in a Room database. Images are copied into the app's private files directory (`filesDir/photos`).
- No `INTERNET` permission is declared — the app is fully offline.
- Camera capture uses `FileProvider` (see `res/xml/file_paths.xml`).
- Gallery picking uses the Android Photo Picker, which requires no runtime storage permission.
- Launcher icons under `mipmap-*` are placeholders — replace before shipping.
