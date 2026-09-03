# MacroTrack

MacroTrack is a comprehensive macro and calorie tracking application built with **Kotlin Multiplatform** and **Compose Multiplatform**, targeting both Android and iOS. It helps users manage their nutrition goals, track daily intake, and monitor their weight progress.

## 🚀 Features

- **Onboarding & Goal Setting**: Personalized setup including activity level, sex, and weight goals to calculate target macros and TDEE.
- **Daily Food Log**: Track your daily consumption of proteins, carbs, and fats. Navigate through historical logs with ease.
- **Meal Management**:
    - Search and add foods or recipes to your daily log.
    - Customize portions and weights for accurate tracking.
- **Custom Content Creation**:
    - Create your own food entries with detailed macro information.
    - Build recipes by combining multiple ingredients.
- **Profile & Progress Tracking**:
    - Monitor your BMI and weight history.
    - Visual weight history graphs to track progress over time.
    - Manage personal information and activity levels.
- **Offline Support**: Fully functional offline using a local database.

## 🛠 Tech Stack

- **Kotlin Multiplatform**: Shared business logic and UI across Android and iOS.
- **Compose Multiplatform**: Declarative UI framework for shared screens.
- **Room Database**: Local persistence for food logs, recipes, and user data.
- **Koin**: Lightweight dependency injection framework.
- **Navigation Compose**: Type-safe navigation for shared UI.
- **Kotlinx Serialization**: JSON parsing and serialization.
- **Kotlinx DateTime**: Handling date and time operations across platforms.

## 📂 Project Structure

- `composeApp/src/commonMain`: Shared Kotlin code including UI (Compose), ViewModels, Repositories, Use Cases, and Domain models.
- `composeApp/src/androidMain`: Android-specific implementations and entry point (`MainActivity`).
- `composeApp/src/iosMain`: iOS-specific implementations and entry point (`MainViewController`).
- `iosApp`: The Xcode project for the iOS application.

## 🛠 Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- Xcode (for iOS development)
- JDK 11 or higher

### Build and Run

#### Android
You can run the app from Android Studio or via the terminal:
```bash
./gradlew :composeApp:assembleDebug
```

#### iOS
1. Open the `iosApp` directory in Xcode.
2. Select a simulator or physical device.
3. Build and Run (Cmd + R).

Alternatively, use the run configurations provided in Android Studio if the KMP plugin is installed.

---

*Developed with ❤️ using Kotlin Multiplatform.*
