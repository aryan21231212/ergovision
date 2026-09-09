# Product Requirements Document (PRD) — ErgoVision

## 1. Problem Statement
Repetitive strain injuries and work-related musculoskeletal disorders (WMSDs) represent the leading category of occupational health claims on factory assembly lines. Existing computer vision solutions require streaming raw video to cloud servers—a model rejected by manufacturing unions and labor regulators (e.g., India's Factories Act 1948, Sections 11–18) due to severe privacy violations. Wearable motion-capture rigs are cost-prohibitive ($10k–$50k/station), fragile, and cumbersome for workers during 8-hour shifts.

## 2. Product Vision
ErgoVision is an offline, privacy-by-architecture ergonomic hazard detector deployed on standard mobile devices (iQOO loaner hardware). Mounted at workstations in a side-view profile, it continuously computes biomechanical risk metrics on-device, provides instant vocal correction for sustained hazardous postures, logs compliance metrics locally, and integrates with supervisor workstations via Office Kit without transmitting video.

## 3. Target User & Setting
* **Primary End-User:** Factory assembly line workers performing repetitive manual tasks.
* **Secondary User:** Environmental Health & Safety (EHS) officers and plant floor supervisors monitoring ergonomic compliance.
* **Deployment Setting:** Workstation side-mount, stationary phone, fixed field of view (FOV) at ~1.5–2.5 meters.

## 4. MVP Goals
1. **Throttled Real-Time Ingestion:** Camera capture locked to ~5 FPS using CameraX 1.5.0+ to prevent thermal saturation.
2. **On-Device Pose Estimation:** Sub-20ms 3D landmark extraction via MediaPipe Tasks Vision (`pose_landmarker_lite.task`) running on the GPU delegate.
3. **Biomechanical Angle Analysis:** REBA-inspired joint-angle calculation using true 3D world landmarks (hip-origin, metric coordinates).
4. **Time-Based Hazard Debouncing:** Sliding time-window with median scoring (5-second sustained hazard trigger, 10–60s alert cooldown) to eliminate transient noise.
5. **Two-Tier Voice Feedback:**
   * **Tier 1 (Instant):** Deterministic, hardcoded template audio alerts via Android TTS (<50ms trigger).
   * **Tier 2 (Async Coaching):** Local LLM-generated contextual feedback via LiteRT-LM (Llama 3.2 1B / Gemma3-1B) post-event.
6. **Local Persistence:** Edge logging of structured hazard events in Room DB (zero images or video frames persisted).
7. **Cross-Device Compliance Loop:** Export weekly compliance summaries to laptop via Office Kit Shared Clipboard and Room DB logs as CSV via Office Kit File Transfer.
8. **Privacy Certification:** Zero network permissions (`android.permission.INTERNET` omitted) and zero persistent external storage permissions.

## 5. Non-Goals (Strict Hackathon Exclusions)
* Full clinical REBA scoring (handling external load weights, coupling scores, dynamic activity factors).
* Multi-person posture tracking or crowd analytics.
* Front-view or 360-degree rotational posture analysis.
* Cloud synchronization, remote telemetry, or user account authentication.
* Live NPU custom binary compilation (QNN/Hexagon SDK manual compilation live during 30h window).
* Complex multi-camera synchronized tracking.
