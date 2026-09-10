# Hackathon Task Execution Plan (TASKS.md)

## Phase 0: Setup & Logistics Validation (Saturday Morning Pre-Code)
- [x] Task 0.1: Check-in: Confirm Red Light operating model (Scenario A: Remote Control to laptop vs. Scenario B: Native on-phone coding).
- [x] Task 0.2: Check-in: Confirm HackTracker metric criteria for on-device AI (NPU vs GPU delegate).
- [x] Task 0.3: Pair iQOO loaner device with laptop via Office Kit (verify Screen Mirror, Shared Clipboard, File Transfer).
- [x] Task 0.4: Create clean Android Studio project with locked dependencies and zero network permissions.
- [x] Task 0.5: Copy `pose_landmarker_lite.task` and quantized LLM weights into project `assets/`.

## Phase 1: Red Light Sprint — Perception & Alert Core Loop (Hours 1–10)
- [x] Task 1.1: Implement `CameraForegroundService` with `FOREGROUND_SERVICE_CAMERA` and persistent notification.
- [x] Task 1.2: Build `CameraXManager` with 5 FPS `SessionConfig` throttling and manual `SystemClock` fallback gate.
- [x] Task 1.3: Integrate `MediaPipePoseEstimator` using `pose_landmarker_lite.task` on Qualcomm GPU delegate with `detectAsync()`.
- [x] Task 1.4: Implement `RebaPoseAnalyzer` 3D vector math (trunk flexion, neck flexion, shoulder abduction).
- [x] Task 1.5: Build `HazardDebouncer` with 5-second sliding timestamp window, rolling median filter, and 10s cooldown.
- [x] Task 1.6: Initialize offline `TextToSpeech` engine and build `TtsAlertManager` with deterministic template sentences.
- [x] Task 1.7: Create Jetpack Compose HUD overlay showing live skeleton, joint angles, and hazard status banners.
- [x] Task 1.8: **MILESTONE FREEZE:** Validate full loop (worker bends >60° for 5 seconds -> instant voice alert fires). Rehearse for Evening Checkpoint.

## Phase 2: Green Light Sprint — Edge AI & Office Kit Integration (Hours 11–22)
- [x] Task 2.1: Implement Room database (`HazardEvent`, `HazardEventDao`, `HazardDatabase`).
- [x] Task 2.2: Wire `HazardDebouncer` state triggers to automatically persist `HazardEvent` records into Room.
- [x] Task 2.3: Integrate `LiteRtLmCoach` using LiteRT-LM Kotlin API; implement async post-hazard coaching prompt.
- [x] Task 2.4: Implement weekly compliance summary prompt querying Room and synthesizing a 3-sentence EHS report via LiteRT-LM.
- [x] Task 2.5: Build Office Kit Shared Clipboard bridge: export generated weekly summary to laptop clipboard on button press.
- [x] Task 2.6: Build Office Kit File Transfer bridge: export Room database records as a formatted CSV file.
- [x] Task 2.7: Implement thermal monitoring via `PowerManager.OnThermalStatusChangedListener` to throttle FPS to 2 on thermal rise.

## Phase 3: Polish, Verification & Pitch Rehearsal (Hours 23–30)
- [x] Task 3.1: Verify privacy compliance: execute `adb shell dumpsys package com.ergovision.app | grep permission` to prove zero internet permissions.
- [x] Task 3.2: Rehearse live demonstration: side-mount phone, mirror screen via Office Kit, trigger hazard, demonstrate instant TTS, sync clipboard summary to laptop.
- [x] Task 3.3: Prepare pitch presentation incorporating India's Factories Act 1948 legal compliance story and hardware ROI metrics.
