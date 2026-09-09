# Project Glossary (GLOSSARY.md)

* **3D World Landmarks:** Real-world 3D metric coordinates output by MediaPipe PoseLandmarker (origin at hips, coordinates in meters) that remain invariant to camera focal length and perspective distortion.
* **CameraX 1.5+:** Modern Android camera jetpack library providing explicit target frame-rate ranges via `SessionConfig` for deterministic capture throttling.
* **Cooldown:** A mandatory suppression period (10–60 seconds) following a hazard alert during which vocal alerts are silenced to avoid alert fatigue.
* **ImageProxy:** Android CameraX in-memory wrapper for image buffers. Must be closed immediately via `.close()` to return the buffer to the camera pipeline.
* **LiteRT-LM:** Google's official, actively maintained on-device LLM runtime (`com.google.ai.edge.litertlm`), replacing legacy MediaPipe LLM Inference APIs.
* **Office Kit:** OEM multi-screen connectivity suite enabling Screen Mirroring, Shared Clipboard, File Transfer, and Remote Control between iQOO phone and laptop.
* **PoseLandmarker Lite:** MediaPipe lightweight pose estimation task model (`pose_landmarker_lite.task`, ~9.5MB) optimized for real-time mobile GPU execution.
* **REBA (Rapid Entire Body Assessment):** Biomechanical risk analysis framework assessing postures of trunk, neck, legs, and upper limbs in industrial workspaces.
* **Red Light Phase:** Hackathon phase (55% duration) during which direct laptop access is restricted, and coding/interaction occurs exclusively on or through the phone.
* **Green Light Phase:** Hackathon phase (45% duration) with unrestricted dual-device access for integration, polish, and pitch prep.
* **Rolling Median Filter:** Statistical filter applied across a sliding time window of hazard scores to filter out momentary tracking jitter or single-frame occlusions.
* **Time-to-First-Token (TTFT):** Duration between submitting a prompt to LiteRT-LM and emitting the first generated output token.
* **Tier 1 Alert:** Instant, hardcoded vocal template triggered deterministically within <50ms of hazard confirmation via offline Android TTS.
* **Tier 2 Coaching:** Non-blocking, contextual feedback synthesized asynchronously by an on-device 1B LLM following incident resolution.
