# MummyFlix - Android TV / Fire TV Stick App

A full-featured IPTV player for Android TV and Amazon Fire TV Stick, converted from the SR-Plus web application.

## Features

- **Live TV Streaming**: HLS stream playback via ExoPlayer
- **Radio Mode**: Audio-only playback with visualizer
- **Channel Management**: Favorites, locks, delete channels
- **Auto Scan**: Simulated satellite channel scanning
- **Program Guide**: Multi-level menu system (TV List, Radio List, Program Setup, Edit, Info, Language, System Settings, Game, System Info)
- **Built-in Games**: Snake game playable with remote
- **Full Remote Support**: D-Pad, OK, Back, Menu, Info, Channel Up/Down, Numeric keys (0-9)
- **Channel Banner**: Info overlay in fullscreen mode
- **Numeric Channel Input**: Direct channel entry via number keys
- **Branding**: MummyFlix branding with "Developed by Sanchit" watermark

## UI/UX - Exact Match to Web Version

- Retro set-top box aesthetic with cyan/blue gradients
- Custom hardware-accelerated bidirectional scroller
- SR PLUS watermark (shown only during video playback)
- Radio mode with animated equalizer bars
- OSD frame with header/footer key hints
- Fullscreen mode hides all OSD elements

## Requirements

- Android Studio Koala (2024.1.1) or later
- JDK 17
- Android SDK 34
- Min SDK: 21 (Android 5.0)
- Target SDK: 34 (Android 14)

## Building the APK

### Local Build

```bash
# Clone the repository
git clone <repository-url>
cd MummyFlix

# Build debug APK
./gradlew assembleDebug

# Build release APK (signed)
./gradlew assembleRelease
```

### GitHub Actions (Automated)

The project includes a GitHub Actions workflow (`.github/workflows/build-and-release.yml`) that:

1. **Builds** both debug and release APKs on every push
2. **Runs** lint and unit tests
3. **Performs** semantic release on main branch pushes
4. **Publishes** release APKs to GitHub Releases when tags are pushed (v*)

#### Semantic Release

The project uses [semantic-release](https://github.com/semantic-release/semantic-release) for automated versioning and changelog generation.

Commit message format (Conventional Commits):
```
feat: add new feature
fix: bug fix
docs: documentation changes
style: formatting changes
refactor: code refactoring
test: adding tests
chore: maintenance tasks
```

Breaking changes:
```
feat!: breaking change description

BREAKING CHANGE: description of breaking change
```

## Project Structure

```
MummyFlix/
├── app/
│   ├── src/main/
│   │   ├── java/com/mummyflix/
│   │   │   ├── model/          # Data models (Channel, AppSettings, ViewState, etc.)
│   │   │   ├── repository/     # ChannelRepository (playlist loading)
│   │   │   ├── preferences/    # DataStore preferences
│   │   │   ├── viewmodel/      # MummyFlixViewModel (UI state & logic)
│   │   │   ├── ui/             # Compose UI components
│   │   │   │   ├── theme/      # Material3 theme
│   │   │   │   ├── ChannelListOSD.kt
│   │   │   │   ├── MainMenuOSD.kt
│   │   │   │   ├── SubMenuOSD.kt
│   │   │   │   ├── AutoScanOSD.kt
│   │   │   │   ├── VideoPlayerLayer.kt
│   │   │   │   └── FullscreenAndOverlays.kt
│   │   │   ├── MummyFlixApplication.kt
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── drawable/       # Vector drawables & shapes
│   │   │   ├── values/         # Colors, strings, themes
│   │   │   ├── xml/            # Backup & data extraction rules
│   │   │   └── mipmap-*        # App icons (MummyFlix logo)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── .github/workflows/
│   └── build-and-release.yml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Fire TV Stick Compatibility

- **Leanback Launcher**: App appears in Fire TV home screen
- **Remote Navigation**: Full D-Pad, OK, Back, Menu, Info key support
- **Hardware Acceleration**: ExoPlayer uses MediaCodec for smooth playback
- **Memory Optimization**: Large heap enabled, ProGuard/R8 shrinking
- **Landscape Only**: Fixed landscape orientation for TV
- **Immersive Mode**: Full-screen, hides system bars

## Playlist Source

Default playlist: `https://iptv-org.github.io/iptv/countries/in.m3u` (Indian channels)

To change the playlist, modify `PLAYLIST_URL` in `ChannelRepository.kt`.

## Branding

- **App Name**: MummyFlix
- **Developer**: Sanchit
- **Logo**: Custom MummyFlix logo (in mipmap folders)

## License

Proprietary - Developed by Sanchit