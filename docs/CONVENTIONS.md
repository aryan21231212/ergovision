# Coding Conventions & Project Rules (CONVENTIONS.md)

## 1. Package & Directory Structure

```
com.ergovision.app/
|-- camera/             # CameraX initialization, frame throttling, ImageAnalysis
|-- pose/               # MediaPipe PoseLandmarker wrapper, GPU delegate setup
|-- math/               # 3D Vector arithmetic, REBA angle calculations
|-- debouncer/          # Sliding time-window, rolling median, state machine
|-- tts/                # Android TextToSpeech engine, utterance listener, audio queues
|-- llm/                # LiteRT-LM runtime manager, prompting, async worker
|-- data/
|   |-- entity/         # Room Database entities
|   |-- dao/            # Room DAOs
|   |-- model/          # Domain data classes, Enums
|   +-- repository/     # Data repositories & CSV export builders
|-- ui/
|   |-- components/     # Camera preview, skeleton overlay canvas, metric HUD
|   |-- theme/          # Material3 color schemes, typography
|   +-- viewmodel/      # MainViewModel managing UI states and flows
|-- bridge/             # Office Kit clipboard and file transfer utilities
+-- service/            # CameraForegroundService (Android 14 camera foreground)
```

## 2. Naming Standards
* **Classes & Interfaces:** `PascalCase` (e.g., `RebaPoseAnalyzer`, `HazardEventDao`).
* **Functions & Variables:** `camelCase` (e.g., `calculateTrunkAngle()`, `isHazardous`).
* **Constants & Enums:** `UPPER_SNAKE_CASE` (e.g., `TARGET_FPS`, `TRUNK_FLEXION_MODERATE`).
* **Compose Functions:** `PascalCase` nouns (e.g., `CameraPreviewView`, `PostureMetricHud`).

## 3. Concurrency & Coroutine Scopes
* `Dispatchers.Default`: Heavy CPU math, 3D vector calculations, rolling median filters.
* `Dispatchers.IO`: Room database queries/inserts, file writing, CSV serialization.
* `Dispatchers.Main`: UI updates, TTS utterance requests, Toast/Snackbar notifications.
* **Rule:** Never launch unmanaged coroutines via `GlobalScope`. Always bind to `viewModelScope`, `lifecycleScope`, or structured `CoroutineScope(SupervisorJob() + Dispatchers.Default)`.

## 4. Resource Cleanup Rules
* **ImageProxy Invariant:** Every `ImageProxy` received in `ImageAnalysis.Analyzer` MUST be closed inside a `finally` block:
  ```kotlin
  try {
      // process frame
  } finally {
      imageProxy.close()
  }
  ```
* **Engine Teardown:** `PoseLandmarker`, `LiteRtLm`, and `TextToSpeech` instances must implement clean teardown methods invoked in `onDestroy()` or `onCleared()`.

## 5. Clean Code for Hackathon Speed
* Avoid deep inheritance hierarchies; prefer pure utility functions for mathematical operations.
* Keep composables dumb; derive visual states strictly from immutable state objects emitted by `StateFlow`.
