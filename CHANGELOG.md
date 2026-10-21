# Changelog

All notable changes to MummyFlix are documented in this file. This file is generated automatically by [semantic-release](https://github.com/semantic-release/semantic-release) from [Conventional Commits](https://www.conventionalcommits.org/).

## [1.2.0](https://github.com/s4nch1tt/MummyFlix/compare/v1.1.0...v1.2.0) (2026-10-21)

### Features

* **ui:** match reference OSD and fix Fire TV navigation ([77c0bbf](https://github.com/s4nch1tt/MummyFlix/commit/77c0bbf6c7edadd10e2ca54815bee06da226c770))

## [1.1.0](https://github.com/s4nch1tt/MummyFlix/compare/v1.0.0...v1.1.0) (2026-10-21)

### Features

* **ui:** complete UI overhaul matching SR-Plus design with focus navigation fix ([ba37a2f](https://github.com/s4nch1tt/MummyFlix/commit/ba37a2ffa93c57b8af10592018d1c396c096882b))

### Bug Fixes

* **build:** add focusArea property to ViewModel for UI navigation ([0ea5623](https://github.com/s4nch1tt/MummyFlix/commit/0ea56234e93aa22decadeadcc64029e5914c7204))
* **build:** add missing Compose imports for Modifier and Composable ([54cb2b0](https://github.com/s4nch1tt/MummyFlix/commit/54cb2b0f212c411c4d1cdd803c4a801755f8d512))
* **build:** remove duplicate MummyFlixBranding composable from VideoPlayerLayer ([4bdb8e6](https://github.com/s4nch1tt/MummyFlix/commit/4bdb8e64dbdb69ede43c8158f45c783db9a892e0))
* **build:** remove duplicate MummyFlixBranding, fix AutoScanOSD footer ([3a91ec2](https://github.com/s4nch1tt/MummyFlix/commit/3a91ec29e552666f1fa30acf60120819e613663f))
* **build:** remove unused RetroOsd.kt and Branding.kt files with compilation errors ([325ae16](https://github.com/s4nch1tt/MummyFlix/commit/325ae168aaf72c898ab915be098a96e940a97346))
* **build:** restore MainActivity to working version ([48c96be](https://github.com/s4nch1tt/MummyFlix/commit/48c96be4bef4d34b4a5c7ad44fa0877f2cd6b75d))

## 1.0.0 (2026-10-20)

### Features

* **ci:** automate versioned releases with semantic-release and refresh docs ([03b474d](https://github.com/s4nch1tt/MummyFlix/commit/03b474d1490c00a60bb0c538351a9be16f55d2d3))
* comprehensive build system overhaul - Gradle 8.9, AGP 8.6, Kotlin 2.0.20, Media3 1.4.1, Compose BOM 2024.10.01, Java 17 ([50d40b8](https://github.com/s4nch1tt/MummyFlix/commit/50d40b8b7d9650f6297b826df5aef54925d7949c))
* initial MummyFlix Android TV / Fire TV Stick app ([2284a52](https://github.com/s4nch1tt/MummyFlix/commit/2284a52c87203e2980a1935d3cb607f54afd5ad6))

### Bug Fixes

* add Material Components and Leanback deps, fix theme parent to MaterialComponents ([9f6dcde](https://github.com/s4nch1tt/MummyFlix/commit/9f6dcdee72ab792a31ce6d12eeaecb2ca645eeaa))
* add missing imports in MainActivity and fix MummyFlixPreferences ([67bc5d6](https://github.com/s4nch1tt/MummyFlix/commit/67bc5d6394177f785d7fa65de7548584f72b61ae))
* add missing Preferences import in MummyFlixPreferences.kt ([a2beb97](https://github.com/s4nch1tt/MummyFlix/commit/a2beb97f337c68bbd88db8cce8aed02d223b0cd7))
* add missing ui-text dependency for FontStyle ([340df99](https://github.com/s4nch1tt/MummyFlix/commit/340df99216f24de854e0f87a10af8a922eb4e226))
* complete rebuild with modern compatible versions - Gradle 8.10, AGP 8.6, Kotlin 2.0.20, Compose BOM 2024.10.01, all deps updated ([9a967df](https://github.com/s4nch1tt/MummyFlix/commit/9a967df6ac684b87184a72ae3af53f62f61865a4))
* **deps:** pin newest library set compatible with compileSdk 36 / AGP 8.13.2 ([dbc3520](https://github.com/s4nch1tt/MummyFlix/commit/dbc3520790b0cd9a93e5930d8d2cc88434ca053f))
* enable AndroidX in gradle.properties ([1cdd7d9](https://github.com/s4nch1tt/MummyFlix/commit/1cdd7d9b968ecb2bed5a51301924ac92dfb560b2))
* escape XML entity in strings.xml (ampersand) ([ac3e3e1](https://github.com/s4nch1tt/MummyFlix/commit/ac3e3e13d4934eb23d32005e724a941a3074bbbe))
* move signingConfigs before buildTypes to resolve 'release' not found error ([5d0b9ff](https://github.com/s4nch1tt/MummyFlix/commit/5d0b9ff3697916a5d01b747af5cdddebfc3c04f0))
* proper XML entity escaping using Python ElementTree ([e9f8a73](https://github.com/s4nch1tt/MummyFlix/commit/e9f8a739059b711181ce5875825f794c9feaa0f5))
* rename 'return' string resource to 'return_action' (Java reserved keyword) ([ad74f1a](https://github.com/s4nch1tt/MummyFlix/commit/ad74f1a4b9b33984c357e91f5f643f4c8be39e21))
* resolve all Kotlin compilation errors ([515d9fb](https://github.com/s4nch1tt/MummyFlix/commit/515d9fb27a7dc68d4a3ba62df1fcfeffc5c495af))
* resolve remaining compile errors ([7dc96de](https://github.com/s4nch1tt/MummyFlix/commit/7dc96de484ef4ea6961ac5e6936ae8a387d14380))
* update Android SDK setup in workflow to use correct package names ([9ab2c48](https://github.com/s4nch1tt/MummyFlix/commit/9ab2c4848f6a45ef9d55a6078ef3e1c9c883c0cd))
* upgrade minSdk to 23 for androidx.tv compatibility ([3bf6772](https://github.com/s4nch1tt/MummyFlix/commit/3bf6772c055677350e58f0c3d677153f76980923))
* upgrade to AGP 8.6.0, Kotlin 2.0.20, compileSdk 35 for Compose 1.7.3 and androidx.tv 1.1.0 ([9ab0a3f](https://github.com/s4nch1tt/MummyFlix/commit/9ab0a3f43debd086a8bfefe2adaa519f5123f14b))
* use 'or' infix function for bitwise OR in MainActivity, fix FontStyle import ([46f7bca](https://github.com/s4nch1tt/MummyFlix/commit/46f7bcafecd61ac44f81c239bf16b701de8e7467))
* use correct androidx.tv library versions (tv-material:1.1.0, tv-foundation:1.0.0) ([e477ed4](https://github.com/s4nch1tt/MummyFlix/commit/e477ed445a1a2b115b7e9d6ec6de129d0be744e2))
