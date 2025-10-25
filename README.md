# ShowMe App 📱🎵

An Android application (Kotlin) designed to help users discover concerts, parties, and events nearby. This project using Firebase and MVVM architecture.

## 🚀 Overview

ShowMe provides a simple and user-friendly platform for finding live entertainment events. The app retrieves data from Firebase Firestore (with potential future integration of external APIs), sorts it by proximity and date, and presents it in a clean, modern interface inspired by contemporary designs.

## ✨ Key Features

* **Dynamic Event Feed:**
    * Displays a list of upcoming events fetched from Firestore.
    * Automatically sorts events by distance from the user (closest first) and then by date (soonest first).
    * Features a professional "Shimmer" loading effect while fetching data.
* **Event Filtering:**
    * Filters the feed by event type (Concert, Party, Theater, etc.) using an interactive ChipGroup.
* **Detailed Event Card:**
    * Main image (expandable to fullscreen on tap).
    * Event name, artist, date, and location.
    * Visual icon indicating the event type.
    * Quick actions:
        * **Ticket:** Opens a link to purchase tickets (URL from Firestore).
        * **Directions:** Opens a navigation app (Google Maps/Waze) with a route to the event location (based on coordinates).
        * **Playlist:** Searches for and opens an official artist playlist/song on YouTube (using YouTube Data API v3).
        * **Share:** Shares event details via other applications.
        * **Favorite:** Adds/Removes the event from the user's personal favorites list.
* **Favorites System:**
    * Saves favorite events to the user's account (using a Firestore subcollection).
    * Dedicated screen to display all favorited events.
    * Visual indication (filled/empty heart icon) on event cards across all screens.
    * Real-time synchronization of favorites using a SnapshotListener.
* **Advanced Search:**
    * Searches events within the existing data (Firestore) based on free text (artist or venue).
    * Filters by a specific date using a DatePickerDialog.
    * Displays clear messages for edge cases (initial search state, no results found).
* **User Authentication (Firebase Authentication):**
    * Registration and login using email and password.
    * (Base implemented for): Google Sign-In and Phone number authentication.
    * Persistent login state (automatically navigates to the main screen if the user is already logged in).
* **User Profile:**
    * Displays username, email address, and profile picture.
    * Option to update the username.
    * Option to update the password (with re-authentication).
    * Option to upload/change the profile picture (using Firebase Storage).
    * Logout button.
* **Modern User Interface:**
    * Netflix-inspired design (dark theme, red-black color scheme).
    * Rounded event cards with elevation ("floating" effect).
    * Uses a custom font (configurable in Themes).
    * Layouts based on ConstraintLayout and LinearLayout.
    * Utilizes Material Components (Buttons, Chips, TextInputLayouts, BottomNavigationView, CardView).
    * Subtle transition animations (Fade/Slide) between screens.
    * Splash screen displaying the app logo on launch.

## 🛠️ Technologies & Libraries

* **Language:** Kotlin
* **Architecture:** MVVM (ViewModel, LiveData, View Binding)
* **UI:** Android XML Layouts, Material Components Library, ConstraintLayout, RecyclerView, ChipGroup, Glide (for image loading)
* **Navigation:** Android Navigation Component
* **Backend & Database:** Firebase
    * Firebase Authentication (Email/Password, Google, Phone)
    * Firebase Firestore (NoSQL Database)
    * Firebase Storage (Profile image storage)
* **Networking:** Retrofit (for YouTube API calls)
* **APIs:**
    * YouTube Data API v3
    * Google Maps Intent API
    * Fused Location Provider (Android Location Services)
* **UI Effects:** Facebook Shimmer
* **Version Control:** Git, GitHub

## ⚙️ Setup & Running

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/roniltnt/ShowMe-App.git
    cd ShowMe-App
    ```
2.  **Open in Android Studio:** Open the project using Android Studio.
3.  **Firebase Setup:**
    * Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    * Add an Android app to the project with the package name `com.example.showme`.
    * Enable the required services: Authentication (with Email/Password, Google, Phone providers), Firestore Database, Storage.
    * Download the `google-services.json` file from your Firebase project settings.
    * Copy the `google-services.json` file into the `app/` directory of your project in Android Studio.
    * **Important:** Ensure you have added the **SHA-1** and **SHA-256** fingerprints (at least for debug) to your Android app settings within your Firebase project (required for Google Sign-In and Phone Auth).
4.  **API Key Setup:**
    * Obtain an API key for the **YouTube Data API v3** via the [Google Cloud Console](https://console.cloud.google.com/).
    * Open the file `app/src/main/java/com/example/showme/utils/Constants.kt`.
    * Paste your key in the appropriate constant.
    * **Important:** It's recommended not to store API keys directly in source code for production releases. Consider using `local.properties` or the Secrets Gradle Plugin.
5.  **Sync & Build:**
    * Click **Sync Now** if prompted by Android Studio.
    * Build the project: **Build -> Make Project**.
6.  **Run:** Run the app on a device or emulator.
