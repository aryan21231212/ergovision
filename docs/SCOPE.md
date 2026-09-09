# Scope Boundaries & Feature Matrix (SCOPE.md)

To guarantee completion within the 30-hour window, ErgoVision strictly enforces the boundaries below. Features marked OUT-OF-SCOPE must not be implemented under any circumstances unless explicitly approved after all MVP milestones are met.

## 1. Feature Matrix

| Feature Area | In-Scope (MVP Core) | Out-of-Scope (Strict Non-Goals) |
|---|---|---|
| **Camera & Vision** | - Single worker monitoring<br>- Stationary side-profile mount (1.5–2.5m)<br>- Throttled 5 FPS stream (CameraX 1.5)<br>- 3D world landmark detection (Lite model) | - Multi-person tracking<br>- Frontal view / multi-angle views<br>- Dynamic panning camera<br>- 60 FPS high-rate streaming |
| **Biomechanical Logic**| - Trunk flexion angle (vs vertical)<br>- Neck flexion angle (shoulder-ear vector)<br>- Shoulder abduction/flexion (>90°)<br>- Time-based 5s median debounce | - Full clinical REBA scoring (loads/forces)<br>- Hand/wrist ergonomics (strain index)<br>- Leg/foot posture evaluation<br>- Repetitive motion frequency indices |
| **Vocal Feedback** | - Hardcoded template alerts via offline TTS<br>- Cooldown enforcement (10–60s)<br>- Audio alert queuing/suppression | - Cloud-based TTS engines (ElevenLabs/Polly)<br>- Speech-to-text worker input<br>- Multi-lingual dynamic translations |
| **Edge AI / LLM** | - LiteRT-LM (Llama 3.2 1B / Gemma3-1B)<br>- Async post-hazard personalized coaching<br>- Batch weekly summary generation | - Synchronous blocking real-time LLM alerts<br>- 7B+ parameter heavy models<br>- Live LoRA fine-tuning on-device<br>- Custom C++ llama.cpp compilation |
| **Data & Privacy** | - Local Room DB for structured events<br>- Zero network permissions (`INTERNET`)<br>- In-memory frame disposal | - Cloud syncing (Firebase, AWS, Supabase)<br>- Video/photo disk storage<br>- User biometric authentication |
| **Office Kit Integration** | - Screen Mirror (live display)<br>- Shared Clipboard (summary sync)<br>- File Transfer (CSV log export)<br>- Remote Control (Red Light development) | - Custom proprietary network bridges<br>- Wi-Fi direct socket servers<br>- Cloud dashboard portals |

## 2. Phase 2 Stretch Goals (Only if MVP completes before Hour 24)
* **H1: IMU/Orientation Fallback Mode:** Fallback posture tracking via device sensors (`TYPE_ROTATION_VECTOR`) for pocket-worn scenarios.
* **H2: Adaptive Power & Thermal Manager:** Automatic stepping down from 5 FPS to 2 FPS or IMU-only based on `PowerManager` thermal status.
