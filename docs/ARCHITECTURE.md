# System Architecture (ARCHITECTURE.md)

## 1. High-Level Component Architecture

```
+-----------------------------------------------------------------------------------+
|                                  iQOO DEVICE                                      |
|                                                                                   |
|  +---------------------+                                                          |
|  | Hardware Sensors    |                                                          |
|  | - Camera Sensor     |                                                          |
|  | - Audio Subsystem   |                                                          |
|  | - Thermal Engine    |                                                          |
|  +----------+----------+                                                          |
|             | Frame Stream (~5 FPS, RGBA_8888)                                    |
|             v                                                                     |
|  +-----------------------------------------------------------------------------+  |
|  | Ingestion & Preprocessing: CameraXManager                                   |  |
|  | - SessionConfig Target FPS (5 FPS)                                          |  |
|  | - Hardware Gate Fallback (SystemClock delta >= 200ms)                       |  |
|  | - STRATEGY_KEEP_ONLY_LATEST Backpressure                                    |  |
|  +--------------------------------------+--------------------------------------+  |
|                                         | ImageProxy                              |
|                                         v                                         |
|  +-----------------------------------------------------------------------------+  |
|  | Perception Engine: MediaPipePoseEstimator                                   |  |
|  | - pose_landmarker_lite.task running on Qualcomm Adreno GPU Delegate         |  |
|  | - Non-blocking detectAsync() execution                                      |  |
|  +--------------------------------------+--------------------------------------+  |
|                                         | 3D World Landmarks (Meters, Hip-Origin)   |
|                                         v                                         |
|  +-----------------------------------------------------------------------------+  |
|  | Biomechanical Analysis: RebaPoseAnalyzer                                    |  |
|  | - Vector dot product & arccosine angle computations                         |  |
|  | - Trunk Flexion, Neck Angle, Shoulder Abduction Scoring                     |  |
|  +--------------------------------------+--------------------------------------+  |
|                                         | Continuous PostureMetrics               |
|                                         v                                         |
|  +-----------------------------------------------------------------------------+  |
|  | Temporal Filtering: HazardDebouncer                                         |  |
|  | - 5.0-second sliding time window buffer                                     |  |
|  | - Rolling median risk filter                                                |  |
|  | - State Machine: IDLE -> WARNING -> TRIGGERED -> COOLDOWN                  |  |
|  +-------------------+----------------------------------+---------------------+  |
|                      | Triggered Hazard Event           | Post-Hazard Record   |
|                      v                                  v                      |
|  +------------------------------------+   +---------------------------------+  |
|  | Tier 1: TtsAlertManager            |   | Tier 2: LiteRtLmCoach           |  |
|  | - Instant hardcoded template alert |   | - Async edge LLM coaching       |  |
|  | - UtteranceProgressListener gating |   | - Weekly compliance summaries   |  |
|  +------------------------------------+   +----------------+----------------+  |
|                                                            |                   |
|                                                            v                   |
|  +-----------------------------------------------------------------------------+  |
|  | Persistence Layer: Room Database                                            |  |
|  | - HazardEvent Entity (Type, Duration, Peak Angle, Timestamp, Advice)        |  |
|  +--------------------------------------+--------------------------------------+  |
|                                         |                                         |
+-----------------------------------------|-----------------------------------------+
                                          |
                      +-------------------+-------------------+
                      |               OFFICE KIT              |
                      v                                       v
      +-------------------------------+       +-------------------------------+
      |   Shared Clipboard Bridge     |       |     File Transfer Bridge      |
      |   (Weekly Compliance Report)  |       |   (Room Hazard Logs as CSV)   |
      +---------------+---------------+       +---------------+---------------+
                      |                                       |
                      +-------------------+-------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                              SUPERVISOR LAPTOP                                    |
|  - EHS Compliance Dashboard (Browser / Spreadsheet)                               |
|  - Screen Mirror Live View (Monitoring & Evaluation)                              |
+-----------------------------------------------------------------------------------+
```

## 2. Execution Paths & Threading Model
* **Camera Analysis Thread (`Executors.newSingleThreadExecutor()`):** Handles frame drops, format validation, and feeds `detectAsync()`.
* **GPU Delegate Worker Thread (MediaPipe internal):** Executes CNN tensor operations on Qualcomm Adreno GPU.
* **Math & Temporal Filtering (`Dispatchers.Default`):** Executes 3D trigonometry and median filtering over the 5-second buffer.
* **Audio & UI Main Thread (`Dispatchers.Main`):** Dispatches TTS speech synthesis, updates Compose HUD elements.
* **LLM & Persistence Thread (`Dispatchers.IO`):** Runs non-blocking Room inserts, triggers LiteRT-LM token generation, handles CSV exports.

## 3. Data Privacy Wall
No frame data crosses the process boundary:
* `ImageProxy.close()` is guaranteed in `finally` blocks immediately after inference.
* Memory buffers are recycled in-place.
* Persistent database contains only discrete numbers and hazard categories.
