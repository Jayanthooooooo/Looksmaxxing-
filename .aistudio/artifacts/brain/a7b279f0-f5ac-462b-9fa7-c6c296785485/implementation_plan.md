# Lumora - Black Screen Fix & Startup Robustness Plan

This implementation plan addresses the critical issue where the Android emulator preview renders a completely black screen on startup, despite a successful build.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following choices were confirmed during Phase 1 clarification:
> - **Primary Diagnostic Focus**: `MainActivity` and navigation start destination checks.
> - **Firebase Initialization Handling**: Gracefully catch exceptions and show offline guest mode / unconfigured notice.

- **Confirmed Decision 1**: Implement robust exception handling around `FirebaseApp` initialization and session observation in `MainActivity` and `AppNavGraph`.
- **Confirmed Decision 2**: Ensure `SplashScreen` transitions reliably within 1.5 seconds even if session observation or database state is delayed.

---

## 1. Overview & Core Concept

- **What It Does**: Fixes startup rendering blocks by ensuring `MainActivity` and `AppNavGraph` emit visible Jetpack Compose UI without freezing on async flows or unconfigured Firebase services.
- **Key Value**: Guarantees that the streaming emulator immediately renders the Splash or Welcome screen on startup.

---

## 2. Technical Architecture & Analysis

### Component Startup Sequence

```
[ MainActivity.onCreate ]
       │
       ▼ (Try / Catch AppCheck)
[ LumoraTheme & Surface ]
       │
       ▼
[ AppNavGraph (Start: Splash) ]
       │
       ▼ (1.5s timeout / session check)
[ SplashScreen ──► Welcome / Main / Onboarding ]
```

### Planned Code Changes

1. **`MainActivity.kt`**:
   - Ensure `setContent` wraps UI in a visible Material3 `Surface` with explicit background color.
   - Guard Firebase App Check and initialization inside safe `try/catch` blocks.

2. **`SplashScreen.kt` / `AppNavGraph.kt`**:
   - Add a robust timeout (e.g., 1.5 seconds) in `SplashScreen` to ensure `onNavigateNext` is always called even if session state observation lags.
   - Handle unconfigured Firebase gracefully without blocking the UI tree.

---

## 3. Verification Plan

- **Compilation**: Run `compile_applet` to verify zero build errors.
- **Unit Tests**: Run `gradle :app:testDebugUnitTest` to verify local Robolectric unit tests pass.
- **UI Verification**: Verify that the streaming emulator successfully renders the Splash and Welcome screen.
