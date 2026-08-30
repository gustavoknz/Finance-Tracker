# Finance Tracker - Kotlin Multiplatform

![CI](https://github.com/gustavoknz/Finance-Tracker/actions/workflows/ci.yml/badge.svg)

A professional-grade Currency Tracker application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, targeting Android and iOS. This project serves as a showcase for modern mobile architecture, offline-first strategies, and high-performance cross-platform development.

## 🚀 Key Features

- **Real-time Exchange Rates**: Integrated with the [Frankfurter API](https://api.frankfurter.dev/v1/latest) to fetch conversion rates from a base currency to all global currencies.
- **Robust Offline-First UX**: Implements a **Stale-While-Revalidate** strategy. The app shows cached data instantly upon launch while silently refreshing in the background.
- **Smart Caching & TTL**: 
  - Exchange rates stay fresh with a 30-minute TTL.
  - Automatic cache cleanup on startup prunes data older than 7 days, keeping the local database lean.
- **Pinned Currencies**: Favorite your most-used currencies to keep them at the top of the list.
- **Advanced Search**: High-performance, debounced search with instant filtering across currency codes and names.
- **Graceful Error Handling**: Persistent "Offline" indicators notify users when sync fails, allowing continued use of cached data without interruption.
- **Multi-platform UI**: Pixel-perfect shared UI using Compose Multiplatform with native-feeling animations and adaptive layouts.

## 🏗️ Architecture & Best Practices

The project is built on **Clean Architecture** and follows strict **Senior Android Developer** standards:

- **State Boundaries (MVI-Lite)**: Uses a unified `ExchangeRateUiState` and a pure ViewModel with a single `onAction` entry point, ensuring predictable state transitions and easy debugging.
- **KMP Discipline**: Platform-specific logic (formatting, currency symbols) is abstracted behind a `PlatformUtils` interface and provided via DI, keeping `commonMain` pure and testable.
- **Performance Optimized**:
  - Heavy mapping and filtering are offloaded to background dispatchers (`Default`).
  - Search input is debounced to minimize CPU usage.
  - UI models use `@Immutable` and stable keys to skip unnecessary recompositions.
- **Reactive Data Layer**: Single Source of Truth (SSOT) via Room database flows. Repositories coordinate network and local storage seamlessly.
- **Testing Excellence**: 
  - **44+ tests** including Repository/ViewModel unit tests, Room migration tests, and Robolectric-powered Compose UI tests.
  - 100% logic coverage in shared code.

## 🛠️ Tech Stack

- **UI**: Compose Multiplatform
- **Persistence**: Jetpack Room (KMP)
- **Dependency Injection**: Koin (Core, Compose, ViewModel)
- **Networking**: Ktor Client (with Exponential Backoff Retry & Logging)
- **Logging**: Kermit (Structured multiplatform logging)
- **Metrics**: AtomicFU (Thread-safe cache hit/miss tracking)
- **Navigation**: Voyager
- **Serialization**: Kotlinx Serialization
- **Time**: Kotlinx Datetime
- **Concurrency**: Kotlin Coroutines & Flow

## 📂 Project Structure

* `/androidApp`: Android entry point, ProGuard configuration, and release signing setup.
* `/iosApp`: iOS SwiftUI wrapper and entry point.
* `/shared`: The core engine of the application.
  * `commonMain`: Domain logic, MVI State, Shared UI, and Room Database.
  * `androidMain` / `iosMain`: Platform adapters and database builders.

## 🛡️ Security & Ops

- **Configuration Management**: Sensitive values like the Base URL are managed via an injected `AppConfig` interface, keeping secrets out of the logic layer.
- **Code Obfuscation**: Production builds use **R8** minification and specialized ProGuard rules for Room, Ktor, and Serialization.
- **Observability**: Structured logs for every network request and database emission. Built-in metrics tracking for cache performance.
- **CI/CD**: Fully automated GitHub Actions pipeline for build verification, linting (Detekt), and UI test validation.

## 🏃 Getting Started

### Android
1. Open the project in Android Studio.
2. For release builds, copy `secrets.template.properties` to `local.properties` and add your signing info.
3. Select `androidApp` and click **Run**.

### iOS
1. Open the `iosApp` directory in Xcode.
2. Select a simulator or device.
3. Click **Run**.

---

Built with ❤️ using the latest [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) ecosystem.
