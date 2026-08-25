# DriveLock

A local-first Android driving-safety application designed to detect when the user is probably driving and reduce smartphone distractions.

## About

DriveLock combines Android Activity Recognition with short, foreground-only speed verification, asks whether the user is driving, and provides the foundation for later trip tracking and distraction reduction.

## Current MVP

- Permission-aware, low-power vehicle transition monitoring
- Foreground Fused Location speed verification after a vehicle transition
- Driver confirmation, active-drive, trip-summary, history, and settings screens
- Session-scoped driver/passenger decisions with duplicate-prompt suppression
- Foreground trip tracking with an ongoing notification
- Live elapsed time, filtered distance, current speed, average speed, and maximum speed in memory
- Probable trip-end detection combining vehicle exit and sustained low speed
- Debounced `IN_VEHICLE` transitions that lead to driver confirmation
- A Room database and repository boundary for locally saved trips
- No background-location, network, or account permissions

## Permissions

DriveLock requests Activity Recognition only after showing an in-app explanation. Precise foreground location is requested progressively, when probable vehicle movement needs speed verification. Android 13 and newer also receive a contextual notification-permission request when the user confirms they are driving. Background location is not requested.

## How Driving Detection Works

Google Play services' Activity Recognition Transition API reports low-power enter/exit changes for vehicle, walking, running, cycling, and still activities. An `IN_VEHICLE` enter temporarily starts Fused Location updates. DriveLock requires at least three accurate, ordered samples at or above 5.5 m/s over ten seconds before asking whether the user is driving. During an active trip, an `IN_VEHICLE` exit must coincide with speed at or below 1.5 m/s for 60 seconds before the trip ends; renewed speed or vehicle presence cancels that window.

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

Milestone 5 probable trip-end detection is implemented. Driver confirmation starts a location foreground service, and DriveLock ends it only after vehicle exit plus sustained low speed. Traffic-light stops alone do not end a trip. Completed sessions are not persisted yet.

## Roadmap

1. Finalize and persist completed trips through Room.
2. Present real trip summaries and history.
3. Add onboarding and production permission guidance.
4. Incrementally explore distraction-reduction features permitted by Android.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35 installed, or run:

```shell
./gradlew build
./gradlew test
```
