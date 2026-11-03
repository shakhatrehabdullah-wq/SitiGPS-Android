# Siti GPS 1.5: vehicle compatibility panel

Adds battery percentage, remaining energy, range, charge/discharge power, battery temperature and cable state cards. Only actual available values from the public Android Automotive service are displayed. The panel refreshes while open and supports manual connection/permission requests.

This is NOT a verified BYD DiLink integration. A Dolphin 2023 may not expose the public Android Automotive library or grant the required read permissions. In that case every reading remains unavailable. Installation does not grant vehicle access. No OBD adapter, private BYD SDK, cloud-account login or CAN communication is implemented.

The A/C, lighting, door-lock, sunroof and window buttons are intentionally disabled: there is no verified control provider. The reader has no actuator setters and sends no control messages. This follows the agreed initial behavior: expose only working readings; leave unsupported features unavailable.

The WebView message bridge accepts only local HTTPS asset-origin messages from the main frame and supports only snapshot/connect commands. SoundCloud and third-party frames cannot use it. No VIN or other vehicle identifier is requested. Battery percentage is calculated only when both current energy and current usable capacity are available; nominal/new-battery capacity is not substituted.

References: https://developer.android.com/reference/android/car/VehiclePropertyIds and https://developer.android.com/reference/androidx/webkit/WebViewCompat . Android documentation defines available APIs; it does not establish compatibility with the user's specific BYD firmware.

