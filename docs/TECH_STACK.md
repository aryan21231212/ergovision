# Technical Stack Specification (TECH_STACK.md)

| Component | Technology | Version / Artifact | Selection Rationale |
|---|---|---|---|
| **Language** | Kotlin | `1.9.22+` | Official Android standard; coroutines for zero-jank background inference. |
| **Android SDK** | Android OS (Target/Compile) | Compile: `34`, Min: `28`, Target: `34` | Matches iQOO loaner device capabilities (Android 14). |
| **UI Framework** | Jetpack Compose & Material 3 | Compose BOM `2024.02.00+` | Minimal boilerplate for HUD overlay, status banners, and live metrics. |
| **Camera Pipeline** | AndroidX CameraX | `1.5.0-alpha01+` (or latest 1.5+) | Mandatory for guaranteed `SessionConfig` frame rate controls at 5 FPS. |
| **Pose Estimation** | MediaPipe Tasks Vision | `com.google.mediapipe:tasks-vision:0.10.14` | Supported replacement for legacy MediaPipe Pose. Provides 3D world coordinates. |
| **Pose Model** | MediaPipe Pose Lite Task | `pose_landmarker_lite.task` (~9.5 MB) | Sub-15ms inference on Qualcomm GPU; minimal memory footprint. |
| **LLM Runtime** | Google Edge LiteRT-LM | `com.google.ai.edge.litertlm:litertlm-android:0.1.0+` | Google's modern on-device LLM runtime with native Kotlin bindings. |
| **LLM Model** | Llama 3.2 1B (Fallback: Gemma3-1B) | Int4 / Int8 Quantized `.bin` / `.tflite` | 40+ tokens/sec decode speed on Snapdragon platform. |
| **Local Database** | AndroidX Room | `2.6.1` | SQLite object mapping with Kotlin Coroutines/Flow integration. |
| **Text-to-Speech** | Android Native TTS | `android.speech.tts.TextToSpeech` | 100% offline, zero network latency, pre-installed on device. |
| **Concurrency** | Kotlin Coroutines & Flow | `1.8.0` | Structured concurrency across `Default` (math) and `IO` (Room/files). |
| **Inter-Device Bridge** | Office Kit Desktop/Phone Suite | System OEM Package | Screen Mirror, Shared Clipboard, File Transfer, Remote Control. |
| **Build System** | Gradle (Kotlin DSL) | `Gradle 8.4+`, AGP `8.2.2+` | Standard fast build pipeline. |

## Prohibited Dependencies
* **No Cloud/Network SDKs:** Do not import Retrofit, OkHttp, Ktor, Firebase, AWS SDK, or Google Analytics.
* **No Unverified Native Compilers:** Do not introduce NDK/CMake scripts for manual llama.cpp compilation during the hackathon.
