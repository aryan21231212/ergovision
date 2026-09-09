# Hard Constraints & Operating Guardrails (CONSTRAINTS.md)

## 1. Hardware & Thermal Limits
* **Target Frame Rate:** Maximum 5 FPS. Never process camera frames unthrottled. Continuous 30 FPS processing on mobile GPUs induces thermal clock throttling from 680MHz down to 231MHz within minutes.
* **Resolution Limit:** Input camera resolution capped at `640x480` (VGA) or `1280x720` (720p). Do not configure 1080p or 4K streams.
* **Screen Brightness:** Provide screen dimming or black overlay toggle during active continuous monitoring to preserve battery and minimize OLED display heat.
* **Orientation Lock:** Android Activity must be locked to `android:screenOrientation="portrait"` in `AndroidManifest.xml` to prevent MediaPipe GPU pipeline crashes during configuration changes.

## 2. Platform & OS Constraints
* **Android 14 Foreground Service:** Continuous camera analysis requires a foreground service with `android:foregroundServiceType="camera"` and permission `android.permission.FOREGROUND_SERVICE_CAMERA`. Failure to do so results in OS termination when the app loses focus.
* **Background Task Whitelisting:** Disable battery optimization on OriginOS/Android for the application during check-in to prevent background process termination.

## 3. Latency Budgets
* **Frame Ingestion to Pose Landmarker:** <= 20ms.
* **Angle & Biomechanical Calculation:** <= 5ms.
* **Sliding Window Median Computation:** <= 2ms.
* **Tier 1 TTS Audio Trigger:** <= 50ms from debounce trigger.
* **Tier 2 LLM TTFT (Time-to-First-Token):** Asynchronous background queue; must not impact camera frame ingestion.

## 4. Privacy & Regulatory Boundaries
* **Forbidden Permissions:** Under no circumstances should `android.permission.INTERNET`, `android.permission.READ_EXTERNAL_STORAGE`, or `android.permission.WRITE_EXTERNAL_STORAGE` be declared in `AndroidManifest.xml`.
* **Zero Visual Persistence:** No image frames, bitmaps, or video clips may be saved to SQLite, Room, Cache, or Internal Storage. Only structured metrics (`HazardEvent`) are legal to persist.
