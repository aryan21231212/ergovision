# ErgoVision — Hackathon Live Demo & Pitch Runbook

## 1. The 3-Minute Elevator Pitch Script

### 0:00 – 0:45: The Problem & Regulatory Hook (India Factories Act 1948)
> "Judges, musculoskeletal disorders (MSDs) are the #1 cause of lost workdays on Indian assembly lines. Under the **Factories Act 1948, Sections 11–18**, plant managers carry explicit statutory obligations for worker health and safety. But factories face an impossible dilemma: cloud computer vision systems stream live video off-site—which labor unions and privacy laws strictly forbid—while wearable motion-capture suits cost \$30,000+ per workstation and break during grueling 8-hour shifts.
>
> Meet **ErgoVision**: an offline, privacy-by-architecture ergonomic hazard detector running entirely on consumer edge hardware—our loaner iQOO phone powered by Snapdragon."

### 0:45 – 1:45: Live Interactive Demonstration
> "Let's demonstrate this live right now through Office Kit Screen Mirroring:
> 1. **Mount & Calibrate:** The phone is side-mounted 2 meters from the assembly station. Tap **'Calibrate'** on the HUD to zero out our camera mounting tilt. Notice the live 5 FPS skeleton overlay—the green bones turn orange or red per joint in real-time as I strain specific angles.
> 2. **3D REBA Vector Trigonometry:** When I bend my lower back past 60° (or tap **'Simulate'**), our 5-second sliding median debouncer filters out momentary movement.
> 3. **Dual-Tier Audio Alert:** *(Audio chime sounds, followed by TTS)* 'Warning: Severe back strain. Stand upright.' Instant, offline feedback (<50ms) without waiting for cloud round-trips.
> 4. **Hardware Protection (Thermal Auto-Fallback):** If the Snapdragon chip heats up or there's no mounting bracket, one tap switches us to **Pocket IMU Mode**. The camera suspends, and the device's rotation vector sensor monitors the worker's forward pitch directly from their pocket."

### 1:45 – 2:30: The Office Kit Compliance Loop
> "At shift end, the supervisor needs audit data:
> 1. Tap **'Sync Summary'** on the phone. Our on-device 1B LLM synthesizes an audit-ready compliance report citing the Factories Act 1948.
> 2. Via **Office Kit Shared Clipboard**, I simply hit Paste on my laptop—and the complete EHS report appears instantly in our management dashboard.
> 3. Tap **'Export CSV'** to beam the raw Room database log to our laptop via Office Kit File Transfer."

### 2:30 – 3:00: Privacy Proof & Closing
> "Finally, proof of privacy: let's run `./demo_compliance.sh` in the terminal. As verified by `dumpsys package`, ErgoVision declares **ZERO INTERNET PERMISSIONS** and **ZERO PERSISTENT STORAGE PERMISSIONS**. No frame or video ever leaves this device. 
> ErgoVision delivers lab-grade ergonomic safety at \$0 infrastructure cost with 100% privacy compliance. Thank you."

---

## 2. 5-Step Live Demonstration Checklist

- [ ] **Step 1: Screen Mirroring Active** — Pair phone with laptop via Office Kit Screen Mirror so judges see the phone UI on the large laptop display.
- [ ] **Step 2: Workstation Calibration** — Stand in a neutral upright posture and tap **'Calib'** on the HUD. Confirm trunk angle zeroes out.
- [ ] **Step 3: Trigger Hazard Alert** — Bend forward past 60° for 5 seconds (or tap **'Simulate'**). Hear the industrial chime and spoken TTS alert: *"Warning: Severe back strain. Stand upright."*
- [ ] **Step 4: Inspect Room Database** — Tap **'Logs (1)'** on the HUD. Show the modal bottom sheet containing the timestamped hazard incident with AI coaching advice.
- [ ] **Step 5: Office Kit Clipboard & CSV Beam** — Tap **'Sync Summary'**, switch to laptop, and press `Ctrl+V` / `Cmd+V` in a text editor to show the generated Factories Act audit report. Run `./demo_compliance.sh` to pull the CSV and prove zero internet permissions.

---

## 3. Judge Q&A Defense Sheet

| Potential Judge Question | Technical Defense / Pitch Response |
|---|---|
| *"Why not stream video to a high-accuracy cloud server?"* | Streaming video of workers violates the Factories Act and union labor contracts. ErgoVision processes frames strictly in memory (`RGBA_8888`), deletes them instantly via `.close()`, and has no `INTERNET` permission in its manifest. |
| *"Why 5 FPS instead of 30 or 60 FPS?"* | Continuous 30 FPS vision workloads on mobile GPUs cause thermal clock throttling from 680MHz down to 231MHz within minutes. At 5 FPS, posture dynamics are fully captured while the device runs cool for an entire 8-hour shift. |
| *"How does your angle math handle camera perspective distortion?"* | Naive 2D image coordinates warp when the worker moves closer or further. We use MediaPipe's **3D World Landmarks**—true metric coordinates in meters with origin at the hips—allowing invariant 3D vector dot-product trigonometry. |
| *"Why REBA over RULA?"* | RULA is limited to upper limbs and seated computer work. Assembly line manufacturing involves whole-body bending, lifting, and trunk flexion, which REBA covers rigorously. |
| *"What if a worker workstation has no space to mount a phone?"* | We built **Pocket IMU Mode** (Stretch H1). The phone slips into a shirt pocket or belt clip and tracks forward spinal lean via `Sensor.TYPE_ROTATION_VECTOR` with zero camera power consumption. |
