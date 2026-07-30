# StreamHub TV

A premium, production-ready Android Live TV streaming application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM**, and **Media3 ExoPlayer**. Runs on phones, tablets, Android TV, and TV boxes from a single codebase.

---

## ✨ Highlights

- **Zero hardcoded channels.** Every channel is loaded at runtime from a `channels.json` file you host on GitHub (or anywhere reachable by URL). Add, remove, rename, enable/disable, or reorder channels by editing that file — no app update required.
- **Adaptive UI**: Bottom Navigation on phones, Navigation Rail on tablets & Android TV.
- **Media3 ExoPlayer**: HLS, DASH, and MP4 playback, fullscreen, Picture-in-Picture, gesture-based volume/brightness, auto-reconnect on error.
- **Offline-first channel cache** via Room — the app keeps working (with the last known channel list) even without a network connection.
- **MVVM + Hilt + Coroutines/Flow + Retrofit + Room + Coil** — a clean, modern, scalable architecture.

---

## 🏗️ Architecture

```
com.streamhub.tv
├── data
│   ├── model          # Channel, ChannelCategory, ChannelsResponse (Kotlinx Serialization)
│   ├── remote         # ChannelApiService (Retrofit, dynamic @Url)
│   ├── local           # Room entities/DAOs (favorites, watch history, channel cache) + DataStore prefs
│   └── repository     # ChannelRepository, FavoritesRepository, WatchHistoryRepository, SettingsRepository
├── di                 # Hilt modules: NetworkModule, DatabaseModule, AppModule
├── ui
│   ├── theme          # Material 3 color scheme, typography, shapes (dark default, light optional)
│   ├── navigation     # Destinations, NavGraph, adaptive Bottom Nav / Nav Rail scaffold
│   ├── components     # ChannelCard, CategoryRow, loading/error states, gradients
│   └── screens
│       ├── home        # Featured, Continue Watching, Favorites, Categories, Recommended
│       ├── livetv       # Category-filtered channel grid
│       ├── player       # ExoPlayer screen (fullscreen, PiP, gestures, auto-reconnect)
│       ├── favorites
│       ├── search       # Instant multi-field search
│       ├── settings     # Theme, auto-update, cache, language, about
│       └── repoconfig   # GitHub raw JSON URL configuration
└── util               # Resource<T>, NetworkConnectivityObserver, DeviceUtils, Constants
```

**Pattern**: MVVM + Repository. ViewModels expose `StateFlow<UiState>`; Composables collect state and never touch data sources directly. All async work uses Kotlin Coroutines/Flow. Dependency injection is handled entirely by Hilt.

---

## 📡 How channel loading works

1. On first launch, the app reads the GitHub raw URL configured in **Settings → Repository Configuration** (defaults to `BuildConfig.DEFAULT_CHANNELS_URL`, set in `app/build.gradle.kts`).
2. `ChannelRepository.fetchChannels()` downloads and parses `channels.json` via Retrofit + Kotlinx Serialization.
3. On success, the JSON is cached in Room (`channel_cache` table) together with the source URL and timestamp.
4. On failure (offline, 404, timeout, malformed JSON), the repository transparently falls back to the last cached payload so the UI never breaks.
5. Pulling to refresh, tapping the Home refresh icon, or re-opening the app re-triggers step 2 — so edits made to `channels.json` on GitHub reach every installed copy of the app without a Play Store update.

### `channels.json` schema

```json
{
  "updatedAt": "2026-07-29T00:00:00Z",
  "channels": [
    {
      "id": "sports_1",
      "name": "Sports 1",
      "logo": "https://.../sports1.png",
      "category": "Sports",
      "streamUrl": "https://.../sports1/index.m3u8",
      "description": "24/7 live sports coverage",
      "country": "Global",
      "language": "Multi",
      "enabled": true
    }
  ]
}
```

A ready-to-use example covering every category requested in the spec (Sports 1–10, News, Movies, Series, Kids, Documentary, Religious, Entertainment — 40 channels total) is provided in [`sample-data/channels.json`](sample-data/channels.json). Upload it to a public GitHub repo, copy its **raw** URL, and paste it into Settings → Repository Configuration.

> ⚠️ The sample file uses a public HLS test stream (`test-streams.mux.dev`) as a placeholder for most entries — replace `streamUrl` values with your real, licensed stream sources before shipping.

---

## ▶️ Player features (Media3 ExoPlayer)

| Feature | Implementation |
|---|---|
| HLS / DASH / MP4 | `Channel.streamType` picks `HlsMediaSource` / `DashMediaSource` / `ProgressiveMediaSource` automatically based on the URL extension |
| Fullscreen | Toggles landscape orientation + immersive system bars (Accompanist SystemUiController) |
| Picture-in-Picture | `Activity.enterPictureInPictureMode()` with a 16:9 `PictureInPictureParams` |
| Volume / brightness gestures | Vertical drag on the right half of the screen = volume, left half = brightness |
| Auto-reconnect | `Player.Listener.onPlayerError` triggers exponential backoff retry, up to 5 attempts, surfaced in the UI |
| Loading state | Buffering spinner driven by `Player.STATE_BUFFERING` |
| Channel overlay | Logo, name, and category shown over the video; next/previous buttons switch within the same category |

---

## 🛠️ Building the project

1. Open the project root in **Android Studio (Koala or newer)** or AndroidIDE.
2. Let Gradle sync (uses AGP 8.5.2, Kotlin 1.9.24, Gradle 8.7, compileSdk/targetSdk 34, minSdk 21).
3. Set your own `channels.json` GitHub raw URL either:
   - at build time, via `DEFAULT_CHANNELS_URL` in `app/build.gradle.kts`, or
   - at runtime, via Settings → Repository Configuration (recommended, since it requires no rebuild).
4. Run on a phone, tablet, or Android TV emulator/device.

### Key dependencies
Jetpack Compose (BOM 2024.06.00) · Material 3 · Navigation Compose · Hilt 2.51.1 · Room 2.6.1 · Retrofit 2.11.0 + Kotlinx Serialization converter · Media3 1.4.0 · Coil 2.6.0 · Accompanist 0.34.0 · TV Compose (`androidx.tv:tv-material`).

---

## 🔒 Notes on network security

`network_security_config.xml` allows cleartext (HTTP) traffic because many public/free IPTV sources are served without TLS. If all of your streams are HTTPS, you can tighten this by removing `cleartextTrafficPermitted="true"`.

---

## 📄 License

This codebase is provided as a starting point for your own product; you are responsible for ensuring you have the rights to any streams/logos you configure in `channels.json`.
