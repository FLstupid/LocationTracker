# LocationTracker

LocationTracker is an Android application for secure, real-time location sharing between trusted contacts (friends and family circles), with offline-first caching and Firebase-backed synchronization.

## Highlights

- Real-time location sharing with foreground tracking service.
- Live map visualization using OpenStreetMap and Leaflet.
- Location history timeline for shared users.
- Family circles, friend management, and activity feed.
- Offline-first data access with Room, then remote sync with Firestore.
- Push notifications with Firebase Cloud Messaging.

## Tech Stack

- Kotlin + Coroutines + Flow
- Jetpack Compose (Material 3)
- Hilt dependency injection
- Room for local persistence
- Firebase Auth, Firestore, Storage, FCM
- OpenStreetMap + Leaflet.js (WebView integration)
- DataStore for app preferences

## Architecture

The project follows Feature-first + MVVM + Clean Architecture:

- `feature/*`: UI screens and view models by feature domain
- `domain/*`: pure Kotlin models, repository interfaces, use cases
- `data/*`: repository implementations, local/remote data sources, mappers
- `core/*`: DI, navigation, utilities, services, base classes

Data flow:

1. UI emits user actions.
2. ViewModel orchestrates state and use cases.
3. Repository writes local cache first (Room), then syncs remote when online.
4. UI observes Flow/StateFlow from local + remote-backed streams.

## Privacy and Security

- Sensitive files are excluded from version control (`.gitignore` hardened for public repositories).
- Runtime secrets are not stored in source control.
- `google-services.json` is required locally and must never be committed.
- Keystores and signing materials are excluded by default.

## Prerequisites

- Android Studio (latest stable)
- JDK 17+
- Android SDK installed
- Firebase project configured for Android package `com.example.locationtracker`

## Local Setup

1. Clone the repository.
2. Create `local.properties` from `local.properties.example` and set your SDK path/API key.
3. Create `app/google-services.json` from your Firebase console.
   - A template is provided in `app/google-services.json.example`.
4. Sync Gradle and build:

```bash
./gradlew assembleDebug
```

## Run and Test

Build and unit tests:

```bash
./gradlew test
```

Lint:

```bash
./gradlew lint
```

Install debug build:

```bash
./gradlew installDebug
```

## Tracking Behavior Notes

- Foreground location permission is required for live updates.
- Background location permission is requested separately on Android 10+ for background tracking continuity.
- Foreground service is started with foreground-safe startup APIs.
- Live updates are cached locally first to reduce data loss during connectivity issues.

## Troubleshooting

- **Map not loading**: verify internet access and WebView availability.
- **No live updates**: confirm location permissions and device location services are enabled.
- **Firebase errors**: verify `google-services.json` matches your package name and project.
- **No background tracking**: check background location permission in app settings (Android 10+).

## Contributing

1. Create a feature branch from `main`.
2. Keep changes scoped and reviewable.
3. Run `test` and `lint` before opening a PR.
4. Do not commit secrets, generated artifacts, or local environment files.

## Roadmap

- Smarter background sync and retry queue visibility
- Better battery-aware tracking policies
- More granular privacy controls for location history visibility

## License

Add your preferred license file (for example MIT or Apache-2.0) before public distribution.
