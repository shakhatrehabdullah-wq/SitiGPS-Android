# Siti GPS 1.4

- Bundled MIT-licensed Leaflet rotation plugin. GPS course-over-ground rotates map tiles and route geometry; the vehicle icon compensates for map bearing.
- Heading updates require motion (4 km/h) and location accuracy of 60 metres or better. Stationary/poor fixes keep the previous heading. Device/screen orientation is not a navigation heading.
- North-up toggle and compass reset. Starting navigation restores heading-up.
- Neon palettes, persisted theme/color, smaller expandable map, responsive portrait/landscape layouts and a five-second animated startup screen.
- The photo remains unavailable; startup uses the existing profile.svg symbol.
- SoundCloud search remains on the actual SoundCloud website in a separate Android WebView. Hide preserves playback; Stop unloads the player. No private/unauthorized catalog API is used.
- Rotation resizes both dashboard and music dialog without recreating the activity.
- Arabic/English virtual keyboard for destination and music query inputs.
- Ambient decorative motion plus a separate opt-in microphone DJ mode. Microphone PCM is analysed locally, not recorded or uploaded; stopping DJ or hiding the page stops the input tracks. It follows nearby audible sound, not SoundCloud's protected digital stream; headphones may not be picked up.

## Online services and limitations

No subscription map provider is integrated. OSM tiles and public OSRM routing require internet and have no availability or live-traffic guarantee. This is a personal-use experimental navigation app, not a replacement for a validated navigation product.

Place search is limited to Jordan, submitted on an explicit button press, cached per query and throttled locally. The Nominatim public service allows at most one request per second across the entire app, requires attribution and an identifying Referer/User-Agent, prohibits autocomplete and bulk queries, and may request that an app switch providers. See https://operations.osmfoundation.org/policies/nominatim/ . For wider distribution deploy a configurable caching/rate-limited proxy or your own search service before launch; local throttling alone does not enforce a global multi-user quota. Map attribution remains visible. See also https://operations.osmfoundation.org/policies/tiles/ .

The APK is a debug build. Updating an existing installed copy requires its original signing key; builds made on different clean machines can have different debug certificates. No installed app is removed automatically.

Physical-device GPS, actual road navigation, SoundCloud playback and changes between cellular networks require testing on an Android phone. Build/lint success does not establish driving accuracy.

