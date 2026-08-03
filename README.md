# Currency Tracker - Kotlin Multiplatform

A simple Currency Tracker application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, targeting Android and iOS.

## 🚀 Features

- **Real-time Exchange Rates**: Integrated with the [Frankfurter API](https://api.frankfurter.dev/v1/latest) to fetch the latest currency conversion rates from Euro to other global currencies.
- **Multi-platform UI**: Shared UI components using Compose Multiplatform.
- **Navigation**: Fluid screen transitions using **Voyager**.

## 🏗️ Architecture & Best Practices

The project follows **Clean Architecture** and **SOLID** principles to ensure maintainability and testability:

- **Domain Layer**: Contains business logic, entities (`ExchangeRate`), and repository interfaces.
- **Data Layer**: Implements repository interfaces. Includes a **Remote Data Source** using **Ktor** for API calls and Previews.
- **Presentation Layer**: Implements the UI using **Jetpack Compose**. Uses **ViewModel** (StateFlow) to manage UI state.
- **Dependency Injection**: Powered by **Koin**, with dedicated modules for shared and platform-specific dependencies.

## 🛠️ Tech Stack

- **UI**: Compose Multiplatform
- **DI**: Koin (Core, Compose, ViewModel)
- **Networking**: Ktor Client
- **Navigation**: Voyager
- **Serialization**: Kotlinx Serialization
- **Asynchronous**: Kotlin Coroutines & Flow

## 📂 Project Structure

* `/androidApp`: Android-specific entry point and configuration.
* `/iosApp`: iOS-specific entry point (SwiftUI wrapper).
* `/shared`: The core of the application where 100% of the logic and UI is shared.
  * `commonMain`: Shared business logic and Compose UI.
  * `androidMain` / `iosMain`: Platform-specific implementations (e.g., Koin platform modules).

## 🖥️ Compose Previews

The project includes fixed Compose Previews for screens that depend on Koin. We use `KoinApplication` in our `@Preview` functions to provide a mock environment, allowing the UI to render correctly in Android Studio.

## 🏃 Running the apps

### Android
1. Open the project in Android Studio.
2. Select the `androidApp` configuration.
3. Click **Run**.
Alternatively: `./gradlew :androidApp:assembleDebug`

### iOS
1. Open the `iosApp` directory in Xcode.
2. Select a simulator or device.
3. Click **Run**.

## 🧪 Running tests

- **Android**: `./gradlew :shared:testAndroidHostTest`
- **iOS**: `./gradlew :shared:iosSimulatorArm64Test`

## 🛡️ Static Analysis (Semgrep)

The project is integrated with **Semgrep** for security-focused static analysis across both Kotlin and Swift.

### Installation
```bash
brew install semgrep
```

### Running Scans
To run a complete scan using recommended community rules for Kotlin and Swift:
```bash
semgrep scan --config p/kotlin --config p/swift --config p/security-audit
```

To use the local configuration:
```bash
semgrep scan --config semgrep.yml
```

---

Built with ❤️ using [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).
