<div align="center">

<br/>

```
  ███████╗██╗  ██╗███╗   ███╗     █████╗ ██╗
  ██╔════╝██║ ██╔╝████╗ ████║    ██╔══██╗██║
  █████╗  █████╔╝ ██╔████╔██║    ███████║██║
  ██╔══╝  ██╔═██╗ ██║╚██╔╝██║    ██╔══██║██║
  ███████╗██║  ██╗██║ ╚═╝ ██║    ██║  ██║██║
  ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝    ╚═╝  ╚═╝╚═╝
```

**A powerful, privacy-first, easy-to-access AI chat app for Android**  
*Built with Jetpack Compose · Multi-model · No ads · No tracking*

<br/>

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![API](https://img.shields.io/badge/Min_API-24-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

<br/>

</div>

---

## ✦ Overview

**ekm AI** is a native Android AI chat application that connects directly to leading AI providers — Google Gemini and OpenAI-compatible APIs — with a beautiful, expressive Material You interface. No middlemen, no subscriptions, no servers. Your data stays on your device.

---

## ✦ Features

### 🤖 AI & Models
- **Multi-model support** — Switch between Google Gemini, OpenAI, and any OpenAI-compatible API
- **Instant response** — Get instant response for your queries
- **Show Reasoning mode** — Optionally appends the model's reasoning process below every response
- **Global system prompt** — Customize AI behavior across all conversations with a full-featured prompt editor
- **Per-model accent colors** — The entire UI tints to match whichever model is active

### 💬 Chat
- **Markdown rendering** — Full in-house markdown parser supporting bold, italic, strikethrough, inline code, fenced code blocks with language labels, headings, bullet & numbered lists, blockquotes, horizontal rules, and links
- **Scrollable code blocks** — Code never wraps or gets clipped; horizontal scroll with `softWrap = false`
- **Copy button** — One-tap copy on every AI message with animated ✓ feedback
- **Model name label** — Each AI bubble shows which model generated it, snapshotted at send time (no stale labels on scroll)
- **Animated typing indicator** — Three-dot wave animation while waiting for response

### 🎤 Voice Input
- **On-device speech recognition** — Uses Android's built-in `SpeechRecognizer`, no API key required, no usage limits
- **Live RMS visualizer** — Animated pulse rings driven by real microphone amplitude in real time
- **Full voice flow** — Listening → Processing → Transcript → Send, all in a beautiful bottom sheet
- **Partial results** — Transcript updates word-by-word as you speak

### 🔐 Security
- **Key encryption** — API keys encrypted at rest using c++ algorithm, never stored in plain text
- **Zero telemetry** — No analytics, no crash reporting services, no tracking of any kind
- **No backend** — All requests go directly from your device to the AI provider

### 🎨 UI & Theming
- **Material You design** — Expressive Material 3 components throughout
- **3 themes** — Light, Dark, and System (follows device)
- **Theme persistence** — Selection saved to SharedPreferences, restored on every launch via `Application` class
- **Full app restart on theme change** — Clean process kill + relaunch via `Process Pheonix lib` for instant theme application
- **Smooth transitions** — Custom slide-up / slide-down / fade animations between all activities
- **Edge-to-edge** — Full edge-to-edge rendering with proper inset handling

### ⚙️ Settings
- **System Prompt Editor** — Monospace editor with live word/char counter, 6 preset templates (Default, Coder, Teacher, Writer, Concise, Friend), unsaved-changes indicator, and animated Save/Discard bar
- **Show Reasoning toggle** — Appends structured reasoning section to AI responses
- **Theme picker** — Full-page theme selector with visual chat previews per theme
- **About page** — App info, feature list, stats, team card
- **Privacy Policy** — In-app privacy policy with structured sections
- **Cache clearing** — One-tap cache clear with redirect to system settings for deeper cleaning

---

## ✦ Tech Stack

<div align="center">

### Core

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android_SDK-3DDC84?style=flat-square&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)

### Networking & Data

![OkHttp](https://img.shields.io/badge/OkHttp-43B02A?style=flat-square&logo=square&logoColor=white)
![JSON](https://img.shields.io/badge/JSON-000000?style=flat-square&logo=json&logoColor=white)
![SharedPreferences](https://img.shields.io/badge/SharedPreferences-FF6F00?style=flat-square&logo=android&logoColor=white)

### Architecture

![MVVM](https://img.shields.io/badge/MVVM-4285F4?style=flat-square&logo=googlecloud&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![StateFlow](https://img.shields.io/badge/StateFlow-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Flow](https://img.shields.io/badge/Flow-7F52FF?style=flat-square&logo=kotlin&logoColor=white)

### Security

![C++](https://img.shields.io/badge/c++-%2300599C.svg?style=for-the-badge&logo=c%2B%2B&logoColor=white)

### AI Providers

![Google Gemini](https://img.shields.io/badge/Google_Gemini-4285F4?style=flat-square&logo=google&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI_Compatible-412991?style=flat-square&logo=openai&logoColor=white)

</div>

---

---

## ✦ Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android device / emulator API 26+
- A Google Gemini API key & OpenAI-compatible API key

### Setup

```bash
# 1. Clone the repo
git clone https://github.com/ekm-labs/ekm_android.git
cd ekm_android

# 2. Open in Android Studio
# File → Open → select the cloned folder

# 3. Build & run
./gradlew assembleDebug
```

---

## ✦ Permissions

| Permission | Reason |
|-----------|--------|
| `INTERNET` | API requests to AI providers |
| `RECORD_AUDIO` | Voice input via on-device speech recognition |
| `ACCESS_NETWORK_STATE` | Network availability indicator in chat |

---

## ✦ Privacy

ekm AI has **no backend infrastructure**. Specifically:

- 🚫 No user accounts or sign-in
- 🚫 No analytics or crash tracking
- 🚫 No data sent to ekm servers (there are none)
- 🚫 No ads, ever
- ✅ All chat history stored locally on-device only
- ✅ All AI requests go directly to the provider you configured

When you send a message, it travels: **Your device → AI Provider API → back to your device.** That's it.

---

## ✦ License

```
The project is under MIT Liscense
```

---

<div align="center">

**Made with ♥ in India**

*ekm AI · ekm Labs · 2026*

</div>
