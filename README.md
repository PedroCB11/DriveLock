# DriveLock

A local-first Android driving-safety application designed to detect when the user is probably driving and reduce smartphone distractions.

## About

DriveLock uses continuous high-accuracy GPS speed monitoring in a foreground service, asks whether the user is driving after crossing 20 km/h, and tracks confirmed trips while reducing selected notification distractions.

## Current MVP

- Permission-aware GPS speed monitoring that continues with the screen off
- Foreground services backed by Fused Location high-accuracy updates
- Driver confirmation, active-drive, trip-summary, history, and settings screens
- Session-scoped driver/passenger decisions with duplicate-prompt suppression
- Foreground trip tracking with an ongoing notification
- Live elapsed time, filtered distance, current speed, average speed, and maximum speed in memory
- Probable trip-end detection combining vehicle exit and sustained low speed
- Automatic Room persistence for finalized trip statistics and endpoints
- Real trip summary and chronological history values
- Three-step, locally persisted onboarding with progressive permission guidance
- Refined Material 3 visual identity and smooth directional screen transitions
- Functional settings for theme, permissions, onboarding review, and local history removal
- Per-app notification limiting during active trips, with privacy-preserving local counts
- Trip history breakdown of avoided notifications by application
- Recoverable UI states for denied, blocked, stopped, and unavailable GPS monitoring
- Driver confirmation after measured speed exceeds 20 km/h
- A Room database and repository boundary for locally saved trips
- No network or account permissions; location remains local to the device

## Permissions

DriveLock progressively requests precise and background location after an in-app explanation so GPS monitoring can continue with the screen off. Android 13 and newer also receive a contextual notification-permission request when a trip begins. Notification-listener access is optional and explained separately before opening Android settings.

## How Driving Detection Works

Fused Location supplies high-accuracy samples through a foreground service. DriveLock asks for driver confirmation when an accurate GPS sample exceeds 20 km/h. During an active trip, speed below that threshold starts a three-minute countdown. Any recovery above the threshold cancels it, preserving the session through traffic lights and congestion; three continuous low-speed minutes end the trip and stop continuous monitoring.

## Architecture

The app uses unidirectional data flow: Compose sends events to ViewModels, ViewModels expose immutable `StateFlow` state, repositories isolate domain models from Room entities, and the detection engine sits behind a replaceable interface. `AppContainer` provides intentionally lightweight dependency wiring.

## Tech Stack

Kotlin, native Android, Jetpack Compose, Material 3, Navigation Compose, Android ViewModel, Coroutines and StateFlow, Room, and Gradle Kotlin DSL.

## Project Structure

```text
app/src/main/java/com/drivelock/app
├── data/          Room and repository implementation
├── detection/     Detection boundary and development fake
├── domain/        Models and repository contracts
├── navigation/    Central routes and navigation host
└── ui/            Screen, UI-state, ViewModel, and theme packages
```

## Current Status

Milestone 11 permission and monitoring recovery is implemented. The home screen now distinguishes a regular location denial, a permanently blocked permission, a paused monitor, and an unavailable GPS service. Each state presents the appropriate retry, settings, or activation action and automatically rechecks access when the app resumes.

## Roadmap

1. Refine detection diagnostics through real-vehicle testing.
2. Incrementally explore distraction-reduction features permitted by Android.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35 installed, or run:

```shell
./gradlew build
./gradlew test
```
