# StreamX (Tube Companion) 📱✨

StreamX (Tube Companion) is a premium, ad-free, offline-ready video and music companion and web controller built entirely in Kotlin and Jetpack Compose. Inspired by the **Elegant Dark** theme, it provides a cinematic, highly-responsive media playback interface with background streaming support, local playlists, offline caching, picture-in-picture simulated mini-player playback, metrics dashboards, and a simulated ad-blocking engine.

---

## 🎨 Design Philosophy: "Elegant Dark"
Inspired by high-resolution home cinematic displays and dynamic light schemes, the visual architecture transitions the app's overall workspace into deep slate backdrops paired with intense crimson and neon blue highlights:
- **Cinematic Workspace (`#0F0F0F`)**: Deepest velvet grey backgrounds that prevent eye strain and reduce power usage on AMOLED screens.
- **Classic Play Crimson (`#FF0000`)**: Strong accented crimson buttons and selected navigation triggers.
- **Stream Blue (`#3EA6FF`)**: Vibrant highlight accents providing instant context for active streaming streams and user avatars.
- **Glass-Morphic Overlays**: Translucent backdrop modals and precise borders (`1px border border-white/5` and `border-white/10`) that make structural panels float beautifully over active content feeds.

---

## 🛠️ Main Feature Set
- 🌐 **Robust YouTube Web Controller**: Embedded full-screen client interface capable of parsing video streams, titles, and routing playback states cleanly.
- 🎵 **Native Player & Wave Synthesizer**: Custom media playback node equipped with realistic track seeking, fluid visualizer waves, and full-screen controls.
- 🗂️ **Offline Shelf Persistence**: Save dynamic resource streams into a local SQL-backed persistence space, allowing simulated download play states without active internet dependencies.
- 📂 **Playlist Custom Manager**: Categorize and group favorite video links, custom tracks, or live streams into dedicated custom playlists.
- 📊 **Metrics & Ad-Block Dashboard**: Monitor saved streaming bandwidth, real-time download indices, total play counters, and active ad-blocking matrices.
- 🎛️ **Floating Mini-Player (PiP)**: Smoothly multi-task with a persistent bottom control bar that lets you dismiss, play, pause, or expand streams from anywhere in the app!

---

## 🏗️ Technology Stack & Architecture
- **Language**: Kotlin Modern JVM Core
- **UI Framework**: Google Jetpack Compose (Declarative UI)
- **Design Guidelines**: Material Design 3 (M3)
- **Data Persistence**: Android Room Database Integration (backed by KSP compile-time code-generators)
- **Asynchronous Flow**: Kotlin Coroutines, StateFlow, SharedFlow, and Lifecycle-aware state collectors (`collectAsStateWithLifecycle`)
- **Structure**: Clean MVVM (Model-View-ViewModel) Architecture promoting strong decoupling of UI components from underlying repository logic.

---

## 🚀 Setup & Execution Manual
StreamX uses the modular Gradle structure. To set up or build the app locally, execute the following commands in your CLI workspace:

```bash
# Verify app syntax and dependencies consistency
gradle compileDebugKotlin

# Build your development APK
gradle assembleDebug
```

---

## 📄 License & Attribution
StreamX is designed as a companion app for video power users. All visual highlights, layouts, and resources were crafted manually to deliver a premier experience.
