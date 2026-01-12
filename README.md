# Finetract

Finetract is a smart Android application designed to automatically track your UPI expenses by analyzing payment notifications and SMS. It helps you stay on top of your daily spending with real-time tracking, budget alerts, and detailed insights.

## features

-   **Automated Expense Tracking**: Automatically detects and logs transactions from popular UPI apps like GPay, PhonePe, Paytm, and BHIM by listening to notifications.
-   **SMS Integration**: Reads transactional SMS as a fallback to ensure no expense is missed.
-   **Privacy First**: All data is stored locally on your device using Room Database. No data is sent to external servers.
-   **Daily Limits & Alerts**: Set a daily spending limit and get notified immediately when you cross it.
-   **Smart Insights**: Visualize your spending habits with charts and detailed analysis.
-   **Modern UI**: Built with Jetpack Compose and Material 3 for a smooth and beautiful user experience.

## Technology Stack

-   **Language**: Kotlin
-   **UI Toolkit**: Jetpack Compose (Material 3)
-   **Architecture**: MVVM (Model-View-ViewModel)
-   **Dependency Injection**: Hilt
-   **Local Storage**: Room Database
-   **Navigation**: Jetpack Navigation Compose
-   **Background Services**: NotificationListenerService, BroadcastReceiver (SMS)

## Setup & Installation

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/RandomRohit-hub/Finetract.git
    ```

2.  **Open in Android Studio**
    -   Launch Android Studio.
    -   Select **Open** and navigate to the cloned `Finetract` directory.

3.  **Sync Project**
    -   Allow Android Studio to download dependencies and sync with Gradle files.

4.  **Run the App**
    -   Connect an Android device (with USB Debugging enabled) or use an Emulator.
    -   Click the **Run** button.

## Usage Guide

1.  **Permissions**:
    -   Upon first launch, grant **SMS Permissions** when prompted.
    -   Grant **Notification Access** within the settings when redirected. This is crucial for the app to detect UPI transactions in real-time.

2.  **Dashboard**:
    -   View your "Today's Spending" and recent transactions on the home screen.

3.  **Set Limits**:
    -   Navigate to the Wallet or Settings section to define your Daily Limit.

4.  **Testing**:
    -   You can test the app by sending a small amount (e.g., ₹1) using any supported UPI app.
    -   Alternatively, use a notification tester app to simulate notifications containing keywords like "Paid", "Sent", or "Debited" followed by an amount (e.g., "₹100").
