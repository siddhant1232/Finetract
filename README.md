# Finetract MVP

A simple Android application to track UPI expenses by listening to notifications.

## How to Run

1. **Open in Android Studio**
   - Open Android Studio.
   - Select **Open** and navigate to this folder: `/Users/apple/Desktop/finetrack`.
   - Click **Open**.

2. **Sync Project**
   - Android Studio will automatically try to sync Gradle.
   - If it doesn't, click the "Elephant" icon (Sync Project with Gradle Files) in the top right toolbar.
   - Wait for the sync to complete.

3. **Run the App**
   - Connect an Android device (Enable USB Debugging) OR create an Emulator (AVD).
   - Click the green **Play** button (Run 'app') in the toolbar.

## Usage

1. **Grant Permission**: 
   - When the app opens, you will see a "Permission Required" screen.
   - Click **Grant Permission**.
   - You will be taken to "Notification Access" settings.
   - Find **Finetract** in the list and toggle the switch to **ON**.
   - Allow the permission confirm dialog.
   - Press **Back** to return to the app.

2. **Dashboard**:
   - You will now see the dashboard with "Today's Spending".
   - You can set a Daily Limit in Settings.

3. **Testing**:
   - To test without a real payment, you can use an app like "Notification Tester" from the Play Store to simulate a GPay notification, or simply make a small transaction (e.g., ₹1) to yourself/friend.
   - The app parses notifications containing: `Paid`, `Sent`, `Transfer`, or `Debited` followed by `₹`, `INR`, or `Rs.`.
