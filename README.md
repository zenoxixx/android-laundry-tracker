# \# 🧺 Laundry Tracker

# 

# An offline-first Android app for tracking clothes sent to laundry or dry-cleaning — batches, service providers, and a reusable closet of clothing items, each with photos.

# 

# Built entirely in Java with Room and MVVM, with zero backend, zero ads, and zero internet permission — everything lives on your device.

# 

# <!--

# 📸 Add a few screenshots here once you have them — this section makes the biggest difference to how the repo reads.

# | Home | Add Laundry | Closet |

# |------|-------------|--------|

# | !\[Home](docs/screenshots/home.png) | !\[Add](docs/screenshots/add.png) | !\[Closet](docs/screenshots/closet.png) |

# \-->

# 

# \## 📥 Download

# 

# Grab the latest APK from the \[Releases page](https://github.com/zenoxixx/android-laundry-tracker/releases/latest) — no Android Studio or building required. Requires Android 8.0 (API 26) or newer.

# 

# > Since this isn't distributed through the Play Store, Android will ask you to confirm installing from an unknown source the first time — this is expected for any app installed outside the Play Store, not a warning specific to this app.

# 

# \## ✨ Features

# 

# \- \*\*Batch tracking\*\* — log what you send to the laundry, who it went to, and its status (given / partially returned / returned)

# \- \*\*Service providers\*\* — save laundromats or dry cleaners you use, with contact details, so you can attach them to a batch in a tap

# \- \*\*Closet\*\* — save clothing items you track often, so re-adding them to a new batch is instant instead of re-entering details every time

# \- \*\*Photos per item\*\* — capture with the camera or pick from the gallery (uses the modern Android Photo Picker, so no storage permission required)

# \- \*\*Backup \& Restore\*\* — export your entire history (batches, services, closet, photos) to a single file, and restore it later by either \*\*merging\*\* it into existing data or \*\*replacing\*\* everything — your choice

# \- \*\*Search\*\* — quickly find a batch by service name or notes

# \- \*\*Fully offline\*\* — no internet permission declared, no accounts, no tracking

# \- \*\*Light \& dark themes\*\* — follows your system setting

# 

# \## 🛠 Built With

# 

# \- \*\*Java\*\*

# \- \*\*Room\*\* — local persistence, with proper versioned migrations (no destructive fallback beyond legacy versions)

# \- \*\*MVVM\*\* architecture with `ViewModel` + `LiveData`

# \- \*\*Material 3\*\* components

# \- Android \*\*Photo Picker\*\* for gallery access, \*\*FileProvider\*\* for camera capture

# 

# \## 📱 Compatibility

# 

# Runs on any Android device on \*\*Android 8.0 (API 26)\*\* or newer. No native code, so no architecture restrictions — one APK works everywhere. Camera is optional; the app installs and works fine on camera-less devices via gallery picking.

# 

# \## 🏗 Architecture

# 

# ```

# com.example.laundrytracker

# ├── db/           # Room database, DAOs, type converters

# ├── model/        # Entities: LaundryBatch, ClothingItem, ClosetItem, LaundryService

# ├── repo/         # Repository layer — single source of truth for the UI

# ├── ui/           # One package per screen (home, add, batch, closet, services, item, viewer)

# │   └── .../      #   Activity + ViewModel pairs, backed by LiveData

# └── util/         # ImageStorage, BackupManager, PermissionUtils, DateUtils

# ```

# 

# Each screen follows the same pattern: an `Activity` observes `LiveData` exposed by its `ViewModel`, which delegates all data operations to a single shared `LaundryRepository`, which in turn talks to Room. No direct database access from the UI layer.

# 

# \## 🚀 Build From Source

# 

# Only needed if you want to modify the code — most people should just use the \[Download](#-download) section above.

# 

# 1\. Clone the repo:

# &#x20;  ```bash

# &#x20;  git clone https://github.com/zenoxixx/android-laundry-tracker.git

# &#x20;  ```

# 2\. Open the folder in \*\*Android Studio\*\* (Ladybug or newer).

# 3\. Let Gradle sync — this needs an internet connection once, to fetch dependencies. The app itself runs fully offline afterward.

# 4\. Run on a device or emulator with \*\*Android 8.0 (API 26)\*\* or higher.

# 

# \## 🔒 Data \& Privacy

# 

# All data is stored locally in a Room database. Photos are copied into the app's private storage (`filesDir/photos`) — never uploaded anywhere. Backups are created and restored entirely on-device; where you save or share the backup file afterward is entirely up to you.

# 

# \## 📄 License

# 

# See \[LICENSE](LICENSE) for details.

# 

# \## 👤 Author

# 

# \[@zenoxixx](https://github.com/zenoxixx)

