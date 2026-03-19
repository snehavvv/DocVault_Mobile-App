# DocVault

DocVault is a secure Android application designed to help users store, manage, and protect their important documents. Built with modern Android development practices, it leverages Jetpack Compose, Hilt, Room, and Biometric authentication to ensure a seamless and secure user experience.

## Features

- **Secure Storage:** Keep your documents safe using local Room database encryption.
- **Biometric Authentication:** Access your vault securely using fingerprint or face unlock.
- **Document Capture:** Easily scan or take photos of documents directly within the app using CameraX.
- **Image Support:** View your stored documents with optimized image loading via Coil.
- **Modern UI:** A clean and responsive interface built entirely with Jetpack Compose.
- **Persistent Settings:** Save your preferences securely with Jetpack DataStore.

## Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Database:** [Room](https://developer.android.com/training/data-storage/room)
- **Navigation:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
- **Asynchronous Programming:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Camera:** [CameraX](https://developer.android.com/training/camerax)
- **Security:** [Biometric API](https://developer.android.com/training/sign-in/biometric-auth)
- **Data Persistence:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)

## Getting Started

### Prerequisites

- Android Studio Ladybug | 2024.2.1 or newer
- Android SDK 35
- Kotlin 2.0.0+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/DocVault.git
   ```
2. Open the project in Android Studio.
3. Build and run the app on an emulator or a physical device (API 26+).

## Project Structure

- `app/src/main/java`: Contains the Kotlin source code organized by feature/layer (Clean Architecture/MVVM).
- `app/src/main/res`: Contains Android resources such as drawables and XML configurations.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
