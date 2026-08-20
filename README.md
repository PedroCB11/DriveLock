# DriveLock

A local-first Android driving-safety application designed to detect when the user is probably driving and reduce smartphone distractions.

## About

DriveLock identifies probable vehicle travel using Android Activity Recognition, asks whether the user is driving, and provides the foundation for later trip tracking and distraction reduction.

## Current MVP

- Permission-aware, low-power vehicle transition monitoring
- Driver confirmation, active-drive, trip-summary, history, and settings screens
- Debounced `IN_VEHICLE` transitions that lead to driver confirmation
- A Room database and repository boundary for locally saved trips
- No GPS, network, account, or background-service permissions

## Permissions

DriveLock requests Activity Recognition only after showing an in-app explanation. On Android 10 and newer this is a runtime permission. Denial leaves the app usable but vehicle monitoring disabled. Location is not requested in this milestone.

## How Driving Detection Works

Google Play services' Activity Recognition Transition API reports low-power enter/exit changes for vehicle, walking, running, cycling, and still activities. DriveLock isolates those callbacks in an activity data source. An `IN_VEHICLE` enter must remain active through a centralized confirmation delay before the state machine asks whether the user is driving; an early exit cancels confirmation.

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

Milestone 1 activity recognition is implemented. The app can detect probable vehicle travel and ask for driver confirmation, while handling missing permission or unavailable Play services without crashing. Trips are not yet recorded because location verification and a real trip lifecycle are later milestones.

## Roadmap

1. Add foreground location and speed verification to strengthen vehicle detection.
2. Build a real foreground trip lifecycle and save completed trips.
3. Improve trip summaries and history presentation.
4. Incrementally explore distraction-reduction features permitted by Android.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35 installed, or run:

```shell
./gradlew build
./gradlew test
```
