<div align="center">

# 🏭 ErgoVision

### Zero-Permission, 100% Offline Edge Ergonomic Hazard Detector for Industrial Assembly Lines

[![Android](https://img.shields.io/badge/Platform-Android%2014%20(API%2034)-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%201.9.22-7F52FF?logo=kotlin&logoColor=white)](#)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](#)
[![MediaPipe](https://img.shields.io/badge/Vision-MediaPipe%20Pose%20(Qualcomm%20GPU)-00C4B4?logo=google&logoColor=white)](#)
[![LiteRT-LM](https://img.shields.io/badge/Edge%20GenAI-LiteRT--LM%20(On--Device)-FF6F00?logo=tensorflow&logoColor=white)](#)
[![Room DB](https://img.shields.io/badge/Storage-Room%20SQLite%20(Local)-F44336?logo=sqlite&logoColor=white)](#)
[![Privacy First](https://img.shields.io/badge/Privacy-Zero%20INTERNET%20Permission-success?logo=shield&logoColor=white)](#hard-constraints)
[![Thermal Aware](https://img.shields.io/badge/Thermal-Throttled%20%40%205%20FPS-orange?logo=speedtest&logoColor=white)](#snapdragon-thermal-architecture)

<br/>

**Built for the iQOO City Battles 2026 (30-Hour Hackathon)**

[Features](#-core-features) • [System Architecture](#-system-architecture) • [UI & UX Showcase](#-responsive-glassmorphic-ui) • [Quickstart & Demo](#-quickstart--demo-runbook) • [Biomechanical REBA Math](#-reba-biomechanical-matrix) • [Statutory Compliance](#-statutory-compliance-factories-act-1948)

</div>

---

## 📌 Problem & Executive Summary

Repetitive strain injuries and Work-related Musculoskeletal Disorders (WMSDs) account for over **40% of all occupational health claims** in manufacturing and assembly plants. Existing camera surveillance solutions stream raw video feeds to cloud servers—violating labor privacy regulations, triggering employee pushback, and failing in air-gapped factory environments. Wearable sensor suits are cost-prohibitive ($10,000–$50,000 per station) and break under harsh shop-floor conditions.

**ErgoVision** repurposes standard **Snapdragon-powered smartphones** (like the iQOO loaner device) into an autonomous, edge-native industrial ergonomics guardian:

* 🔒 **100% Offline & Private:** Zero `INTERNET` and zero external storage permissions declared. No frame, pixel, or biometric landmark ever leaves the handset.
* ⚡ **Snapdragon Thermal Protection:** Throttled to **~5 FPS (200ms)** with dynamic down-clocking to **2 FPS** and auto-fallback to an **IMU Wearable Mode** under high thermal load, preventing GPU clock collapse.
* 📐 **3D REBA Vector Trigonometry:** Calculates true spatial joint vectors using MediaPipe 3D metric world coordinates (hip-origin), making detection invariant to camera distance, tilt, and worker height.
* ⏱️ **Temporal Noise Immunity:** 5.0-second sliding time-window with a rolling median filter eliminates false triggers from fleeting movements (e.g. retrieving a dropped bolt), backed by a 15-second cooldown cycle.
* 🔊 **Two-Tier Audio Guidance:** Pre-alert industrial chime followed by deterministic, low-latency (<50ms) offline Android TTS vocal coaching.
* 🧠 **On-Device GenAI Legal Audit:** Uses **LiteRT-LM** (Llama 3.2 1B / Gemma3-1B) to synthesize EHS compliance audit reports grounded in **India's Factories Act 1948 (Sections 11–18)**.
* 💻 **iQOO Office Kit Synergy:** 1-tap **Shared Clipboard** syncs generated compliance reports directly to the supervisor's laptop; **File Transfer** streams Room DB incident logs as formatted CSV.

---

## 📐 System Architecture

```mermaid
flowchart TD
    subgraph PERCEPTION ["1. Perception & Dual Ingestion Layer"]
        CAM["CameraX (Front/Back)"] -->|"200ms Frame Throttler"| MP["MediaPipe Pose Estimator\n(Qualcomm Adreno GPU Delegate)"]
        IMU["IMU Rotation Vector\n(Sensor.TYPE_ROTATION_VECTOR)"] -.->|"Auto-Fallback on Severe Heat"| IMU_TRACK["ImuPostureTracker\n(Pitch & Roll Delta)"]
    end

    subgraph BIOMECHANICS ["2. Ergonomic Analysis Engine"]
        MP -->|"3D World Landmarks (Metric Meters)"| REBA["RebaPoseAnalyzer (3D Vector Math)\nTrunk Flexion • Neck Angle • Arm Reach"]
        IMU_TRACK -->|"Trunk Tilt Delta"| REBA
        REBA -->|"Continuous Risk Metrics"| DEBOUNCE["HazardDebouncer\n5s Sliding Window + Rolling Median Filter"]
    end

    subgraph ACTION ["3. Instant Audio Alert & Edge Storage"]
        DEBOUNCE -->|"Sustained Hazard (>5s)"| CHIME["ToneGenerator Chime"]
        CHIME --> TTS["Offline TextToSpeech\nInstant Worker Voice Correction (<50ms)"]
        DEBOUNCE -->|"Persist Incident"| ROOM[("Room SQLite DB\nHazardEvent Records")]
    end

    subgraph GENAI ["4. On-Device Edge GenAI"]
        ROOM -->|"Historical Incident Logs"| LLM["LiteRT-LM Coach\n(Llama 3.2 1B / Gemma3-1B)"]
        LLM -->|"Weekly EHS Audit Summary"| MODAL["ComplianceReportDialog\nCiting Factories Act 1948"]
    end

    subgraph OFFICEKIT ["5. iQOO Office Kit Cross-Device Ecosystem"]
        MODAL -->|"1-Tap Sync"| CLIP["Shared Clipboard Bridge\n(Pasted directly to Supervisor Laptop)"]
        ROOM -->|"Export CSV"| FILE["File Transfer Bridge / Share Intent\n(Raw Audit Data Transfer)"]
    end
```

---

## 🛡️ Hard Constraints & Privacy Wall

| Constraint | Enforcement Mechanism | Verification Proof |
|---|---|---|
| **Zero Network Telemetry** | `android.permission.INTERNET` is omitted from `AndroidManifest.xml`. | `adb shell dumpsys package com.ergovision.app \| grep -i internet` returns empty. |
| **Zero Persistent Video Storage** | In-memory frame buffers (`ImageProxy`) closed in `finally` blocks immediately after inference. | Zero image/video files written to disk; verified by zero storage permissions. |
| **Snapdragon Thermal Health** | Ingestion gated to 5 FPS (`200ms`), dropping to 2 FPS (`500ms`) and switching to IMU on `THERMAL_STATUS_SEVERE`. | Sustained battery temp < 38°C; zero GPU throttling collapse. |
| **Offline Autonomous Operation** | Android native TTS, MediaPipe GPU delegate bundle (`.task`), and LiteRT-LM models stored locally in app sandbox. | Works seamlessly in Airplane Mode / Faraday cage. |

---

## 📱 Responsive Glassmorphic UI

The interface has been meticulously designed following a **minimalist, uncluttered industrial aesthetic** that automatically adapts across **all form factors** (compact phones, standard displays, foldables, and tablets) using Jetpack Compose `BoxWithConstraints`:

```
┌──────────────────────────────────────────────────────────┐
│  [ ● SAFE   5 FPS ]               [ 📷   🔄   🎯   🔊   🌙 ]  │  <-- Frosted Top Capsule
├──────────────────────────────────────────────────────────┤
│    TRUNK 12°   │   NECK 8°   │   ARM 22°   │   REBA 1.0     │  <-- Biomechanical Telemetry
├──────────────────────────────────────────────────────────┤
│                                                          │
│                     ● Neck (Safe)                        │
│                    / \                                   │
│            Arm ●──●───●──● Arm (Safe)                    │
│                   │   │                                  │
│         Trunk === │   │ === (Green/Yellow/Red Gradient)   │
│                   ●───● Hips                             │
│                                                          │
├──────────────────────────────────────────────────────────┤
│       [ ✨ EHS Audit ]   [ 📋 Logs (14) ]   [ 📤 CSV ]   │  <-- Floating Bottom Dock
└──────────────────────────────────────────────────────────┘
```

### Key UI Capabilities:
1. **Adaptive Status Capsule**: Live glowing beacon (`● SAFE`, `● ANALYZING`, `● HAZARD ALERT`, `● COOLDOWN`) with real-time FPS/sensor indicator.
2. **Segment-Level Risk Gradient Skeleton**: Individual skeleton bones dynamically shift colors in real time:
   * **Trunk:** Safe (Green <20°), Warning (Yellow 20°–60°), Severe (Red >60°).
   * **Neck:** Safe (Green <20°), Warning (Yellow >20°).
   * **Arms:** Safe (Green <60°), Warning (Yellow 60°–90°), Severe (Red >90°).
3. **Biomechanical Pocket Mode (`PocketModeView`)**: When activated, the camera feed shuts down completely to save power and privacy, revealing a **54° circular tilt dial** with 1-tap baseline calibration.
4. **Interactive EHS Audit Dialog (`ComplianceReportDialog`)**: Modal preview allowing supervisors to review on-device GenAI legal audits before 1-tap copying to the Office Kit Shared Clipboard.
5. **Front / Back Camera Toggle (`🔄`)**: Switch between workstation side-mount (back camera) and personal desk self-testing (front camera) instantly.
6. **OLED Low-Power Screen Saver (`🌙`)**: True black OLED screen with a gentle pulsing beacon to monitor postures during 8-hour shifts with near-zero screen power consumption.

---

## 📊 REBA Biomechanical Matrix

ErgoVision maps joint angles to the clinical **Rapid Entire Body Assessment (REBA)** ergonomics standard:

| Body Segment | Neutral / Safe (Green) | Mild Strain (Yellow) | Severe Hazard (Red) | Alert Audio Trigger |
|---|---|---|---|---|
| **Trunk Flexion** | `0° – 20°` | `20° – 60°` | `> 60°` | *"Warning! Please straighten your back immediately."* |
| **Neck Flexion** | `0° – 20°` | `> 20°` | N/A | *"Caution! Raise your head to neutral alignment."* |
| **Shoulder Abduction** | `0° – 60°` | `60° – 90°` | `> 90°` | *"Warning! Lower your arms below shoulder height."* |

---

## ⚖️ Statutory Compliance (Factories Act 1948)

ErgoVision's on-device **LiteRT-LM** synthesizes reports directly aligned with Indian occupational safety laws:

* **Section 11 (Cleanliness & Ergonomics):** Workstation environment must not induce continuous physical strain.
* **Section 14 (Fatigue & Strain):** Mandates elimination of continuous physiological fatigue that degrades worker longevity.
* **Section 18 (Health & Safety):** Standardizes automated reporting for plant floor compliance reviews.

**Sample Synthesized Audit Output:**
> *"EHS COMPLIANCE MEMORANDUM: Workstation #4 logged 12 severe trunk flexion incidents (>60°) over a 4-hour cycle. Under Section 14 of the Factories Act 1948, immediate adjustment of the component bins (15cm higher) is recommended to prevent lumbar spinal shear."*

---

## 🚀 Quickstart & Demo Runbook

### Prerequisites
* Android Studio Ladybug / Koala / Hedgehog
* Android SDK 34 (Min SDK 28)
* Snapdragon Android phone (e.g. iQOO loaner device) with USB debugging enabled

### Build & Run
```bash
git clone https://github.com/aryan21231212/ergovision.git
cd ergovision
# Open in Android Studio, connect your device, and click Run (Shift + F10)
```

### 🎯 60-Second Hackathon Demonstration Sequence

1. **Stationary Camera Setup:**
   * Launch ErgoVision. Tap **"Calibrate" (`🎯`)** while standing upright to baseline the camera tilt angle.
2. **Trigger Sustained Hazard:**
   * Bend your torso forward (>60°). The top pill transitions from `SAFE (Green)` to `ANALYZING (Yellow)`.
   * Hold the pose for **5 seconds**. An industrial **chime** sounds followed by:
     > *"Warning! Please straighten your back immediately."*
   * The status turns `HAZARD (Red)` and enters `COOLDOWN (15s)` to prevent alarm fatigue.
3. **Inspect Local Incident Logs:**
   * Tap **`📋 Logs`** on the bottom dock to inspect the recorded event, peak angle, duration, and on-device coaching tip.
4. **Generate EHS Audit & Office Kit Sync:**
   * Tap **`✨ EHS Audit`**. An interactive memo appears citing **Sections 11–18 of the Factories Act 1948**.
   * Tap **"Copy to Office Kit"** — the text is now on your laptop's clipboard ready to paste!
5. **Demonstrate Pocket Wearable Mode:**
   * Tap **`📱 Mount`** to switch to **Pocket Mode**. The camera turns off, displaying the real-time **54° circular tilt dial**.
   * Slip the phone into your shirt pocket or belt clip to demonstrate wearable ergonomics!

### Automated Jury Verification Script
To prove 100% privacy compliance and thermal health during judging:

```bash
chmod +x demo_compliance.sh
./demo_compliance.sh
```

**Script Audit Output:**
```
============================================================
    ERGOVISION — PRIVACY & COMPLIANCE VERIFICATION SUITE   
============================================================
--> 1. Checking connected hardware...
  List of devices attached: 98231023 device

--> 2. Verifying Declared Permissions (Privacy-by-Architecture)...
  [PASS] ZERO INTERNET PERMISSION DECLARED. No video data can leave device.
  [PASS] ZERO EXTERNAL STORAGE PERMISSION. In-memory processing verified.
  [PASS] CAMERA hardware permission verified.

--> 3. Checking Foreground Camera Service (Android 14+ Lifecycle)...
  Service active in background: CameraForegroundService (camera)

--> 4. Pulling exported Room DB Hazard Log CSV from device...
  [SUCCESS] Pulled latest_hazard_audit.csv to laptop via bridge.

--> 5. Hardware Battery & Thermal Governor Check...
  [PASS] Battery Temperature: 32.4°C (Nominal thermal state, ~5 FPS throttle active)
  [PASS] Current thermal status: 0 (NONE - Cool)
============================================================
    VERIFICATION COMPLETE: READY FOR JURY DEMONSTRATION     
============================================================
```

---

## 📂 Repository Architecture

```
ergovision/
├── app/src/main/
│   ├── AndroidManifest.xml              # Zero INTERNET, Foreground Camera Service
│   ├── assets/
│   │   └── pose_landmarker_lite.task    # Bundled MediaPipe GPU pose model (5.5MB)
│   └── java/com/ergovision/app/
│       ├── bridge/
│       │   └── OfficeKitBridge.kt       # Shared Clipboard & CSV File Transfer bridges
│       ├── camera/
│       │   └── CameraXManager.kt        # 5 FPS throttled camera pipeline with camera flip
│       ├── data/
│       │   ├── dao/HazardEventDao.kt    # Room DAO with analytical aggregation
│       │   ├── database/HazardDatabase.kt
│       │   ├── entity/HazardEvent.kt    # SQLite incident entity
│       │   └── model/                   # PostureMetrics, Point3D, Point2D, HazardState
│       ├── debouncer/
│       │   └── HazardDebouncer.kt       # 5.0s window + rolling median filter
│       ├── llm/
│       │   └── LiteRtLmCoach.kt         # On-device LiteRT-LM & Factories Act prompt
│       ├── math/
│       │   ├── RebaPoseAnalyzer.kt      # 3D REBA vector math & tilt calibration
│       │   └── VectorMath.kt            # Dot product & 3D angular geometry
│       ├── pose/
│       │   └── MediaPipePoseEstimator.kt# MediaPipe on Qualcomm Adreno GPU Delegate
│       ├── sensor/
│       │   └── ImuPostureTracker.kt     # IMU Wearable pocket/belt clip tracker
│       ├── service/
│       │   └── CameraForegroundService.kt # Continuous foreground monitoring service
│       ├── tts/
│       │   └── TtsAlertManager.kt       # ToneGenerator chime + offline Android TTS
│       └── ui/
│           ├── MainActivity.kt          # Single Activity orchestrator & thermal governor
│           ├── components/
│           │   ├── BottomControlDock.kt # Floating responsive action dock
│           │   ├── ComplianceReportDialog.kt # EHS audit report inspector
│           │   ├── HazardLogSheet.kt    # Incident history inspector sheet
│           │   ├── PocketModeView.kt    # Low-power circular tilt dial
│           │   └── PostureHud.kt        # Adaptive top status pill & skeleton overlay
│           └── theme/                   # High-contrast industrial theme
├── app/src/test/java/com/ergovision/app/
│   ├── debouncer/
│   │   └── HazardDebouncerTest.kt       # Sliding window & median filter unit tests
│   └── math/
│       └── RebaPoseAnalyzerTest.kt      # 3D trigonometry & calibration unit tests
├── docs/
│   ├── DEMO_RUNBOOK.md                  # 3-minute pitch script, live demo runbook & FAQ
│   ├── ARCHITECTURE.md                  # Full architectural specification
│   ├── TASKS.md                         # Phase 0-3 execution checklist
│   └── ... (13 locked engineering specs)
├── demo_compliance.sh                   # Automated jury verification script
└── README.md
```

---

## 🧪 Unit Test Suite Coverage

| Test File | Target Subsystem | Coverage |
|---|---|---|
| [`RebaPoseAnalyzerTest.kt`](file:///Users/aryanpratapsingh/Desktop/ergovision/app/src/test/java/com/ergovision/app/math/RebaPoseAnalyzerTest.kt) | 3D Vector Math & Calibration | Dot product trigonometry, neutral upright baselines, severe trunk flexion (>60°), and camera mount tilt zeroing. |
| [`HazardDebouncerTest.kt`](file:///Users/aryanpratapsingh/Desktop/ergovision/app/src/test/java/com/ergovision/app/debouncer/HazardDebouncerTest.kt) | Temporal Filtering & Cooldown | Safe posture rejection, 5s sliding window coverage (>80%), rolling median noise spike rejection, and 15s cooldown state locking. |

---

## 👥 Authors & Acknowledgments

* **Aryan Pratap Singh** — Lead System Architect & Developer
* Developed for **iQOO City Battles 2026** (30-Hour Hackathon).
* Dedicated to enhancing the physical longevity, health, and dignity of industrial factory assembly workers.
