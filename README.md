# Personal AI

Sky-Dash is now being rebuilt as a mobile-first personal AI assistant for Android.

## Current foundation
- Native Android app (Java)
- Microphone permission
- Android foreground microphone service
- Bangla speech recognition using Android SpeechRecognizer
- Android Text-to-Speech
- Quiet mode and resume command
- First device-intent prototype: Watch connection request
- No Termux:API dependency for the Android app

## Planned architecture
1. Local conversation brain
2. Proactive conversation with cooldowns
3. Safe event/audio detection (including optional cough/activity detection)
4. Explicit memory controls
5. Android foreground/background reliability
6. Wear OS companion app so the same assistant can appear on a supported watch
7. Device handoff: phone ↔ watch
8. Optional local AI model for better natural language
9. Optional cloud AI only if the user chooses it

## Important Android limits
The assistant cannot silently grant Android permissions. When a feature needs microphone, Bluetooth, notifications, or watch access, the app must ask and the user must approve the system permission/connection prompt.

A phone app also cannot literally move itself onto any watch. A supported watch needs its own companion app/service, then the phone and watch communicate securely.

## Build
The repository is designed for Android + Termux development and GitHub Actions builds.
