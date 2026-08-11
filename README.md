# C2004

Legacy-first Android music player for Sony Xperia M Dual C2004 / Android 4.1–4.3.

## Goals
- API 16 minimum, with API 17 as the primary target.
- Classic Android Views; no Compose or Material 3.
- Local MediaStore library and lightweight search.
- Background playback using MediaPlayer + MediaSessionCompat.
- Audio focus, headset/media-button support and notification controls.
- HTTPS compatibility through Conscrypt on legacy Android.
- Optional Subsonic/OpenSubsonic backend for self-hosted streaming.
- Resumable HTTP downloads to app-managed music storage.
- ARMv7-only release configuration to keep the APK small.

## Compatibility
The app is deliberately conservative because the target device has a dual-core 1 GHz CPU, 1 GB RAM, Adreno 305 GPU and 4-inch 854x480 display. Avoid adding modern AndroidX/Media3/Compose dependencies unless their minimum SDK is explicitly verified.

## Build
Use a current Android SDK/Gradle environment to build the APK, then test the resulting APK on an API 16–18 emulator and, most importantly, real C2004 hardware. The emulator cannot reproduce the device's memory and GPU constraints accurately.

## Remote backend
The architecture keeps remote playback behind a backend layer. Subsonic/OpenSubsonic is the intended first-party-compatible backend. Unofficial third-party extraction sources should remain optional adapters rather than being coupled to the player core.

## Status
The repository currently contains the core project, local library scanner, playback service, TLS/network stack, Subsonic client, and resumable download engine. Hardware validation and deeper queue/equalizer/settings UX remain device-test work rather than claims of compatibility made without testing.
