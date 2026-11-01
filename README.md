# Siti GPS Android

Native Android WebView app, designed for landscape full-screen operation. Open this directory in Android Studio; allow Gradle sync and install/run. The repository includes Java source and Gradle build files, but no prebuilt APK or Gradle wrapper. Requires Android SDK 35, JDK 17 and internet for Gradle dependencies, Leaflet/OpenStreetMap tiles, search, OSRM demo routing, and SoundCloud embed.

## Included
- Fullscreen immersive Android experience, real GPS permission and location via Android WebViewAssetLoader HTTPS origin.
- Speed, max speed, approximate distance, GPS accuracy and GPS-derived acceleration and heading. Direction is only accurate while moving and with GPS heading present.
- Left-side speed gauges and right-side interactive map, map-follow and car direction marker.
- Destination text search with Nominatim and in-map route display via public OSRM demo server. Public services are best-effort, not suitable for production traffic or safety-critical turn-by-turn guidance.
- SoundCloud public track/playlist embed from link only. **No internal music catalog search** (requires SoundCloud API authorization). User must initiate playback.
- Animated Siti logo with user-provided profile photo.

## Known limitations
- On small portrait screens it keeps two columns but can be cramped; landscape is recommended and configured.
- No driving voice guidance or automatically rotated map. Heading follows the vehicle marker instead.
- No official RPM, gear or engine data without OBD-II hardware.
- No APK built in this environment: Android SDK and Gradle not installed.
- This app is a rebuilt independent edition, not a byte-for-byte export of the Base44 project.
- The publicly accessible image is baked into the app (profile.jpg). Verify permission for distribution.

## Privacy
Location processed in device webview; for destination routes/search, destination and current coordinates are sent to third party geocoding/routing endpoints. No persistent trip storage.
