# 🧺 Laundry Tracker

Offline Android app for tracking clothes sent to laundry or dry-cleaning — batches, service providers, and a reusable closet of clothing items, each with photos.

Java, Room, MVVM. No backend, no ads, no internet permission.

## Download

[Latest release](https://github.com/zenoxixx/android-laundry-tracker/releases/latest) — Android 8.0+.

## Features

- Batch tracking with status (given / partially returned / returned)
- Service providers with contact info
- Closet of saved clothing items for quick re-adding
- Camera or gallery photos per item
- Backup & restore (merge or replace) as a single exportable file
- Search
- Light/dark theme
- Fully offline

## Built With

- Java
- Room (versioned migrations, no destructive fallback)
- MVVM — `ViewModel` + `LiveData`
- Material 3
- Android Photo Picker, FileProvider

## Architecture

```
com.example.laundrytracker
├── db/       # Room database, DAOs, converters
├── model/    # LaundryBatch, ClothingItem, ClosetItem, LaundryService
├── repo/     # Single repository, source of truth for the UI
├── ui/       # Activity + ViewModel per screen
└── util/     # ImageStorage, BackupManager, PermissionUtils, DateUtils
```

Activities observe LiveData from their ViewModel, which goes through `LaundryRepository` to Room. No direct DB access from the UI layer.

## Build From Source

```bash
git clone https://github.com/zenoxixx/android-laundry-tracker.git
```

Open in Android Studio, sync, run. Min SDK 26.

## License

[LICENSE](LICENSE)

## Author

[@zenoxixx](https://github.com/zenoxixx)