# Happy Places Application

Happy Places is an Android application built using Kotlin that allows users to save and manage their favorite locations. Users can add details such as a title, description, date visited, location coordinates/address, and an image for each "Happy Place."

## Overview

This app provides a simple interface to catalog memorable places. Users can add new places through a dedicated form, selecting images from their gallery or capturing new ones with the camera. Location input is streamlined using Google Places Autocomplete. Saved places are displayed in a list, and users can view details or delete entries with a simple swipe gesture.

## Key Features

*   **Add Happy Places:** Create new entries for favorite locations.
*   **Rich Place Details:** Save Title, Description, Date, Location (Address + Coordinates), and an Image for each place.
*   **Image Selection:** Choose images from the device gallery or capture new photos using the camera.
*   **Date Picker:** Easily select the date associated with the place using a native Android DatePickerDialog.
*   **Location Input:**
    *   Uses Google Places Autocomplete SDK for easy address and location searching.
    *   Stores Latitude and Longitude coordinates along with the address.
*   **Local Storage:** Persists saved happy places using a local SQLite database (`DatabaseHandler`).
*   **Image Management:** Saves selected/captured images to the app's internal storage and stores the URI path in the database.
*   **View Places:** Displays a list of saved happy places using `RecyclerView` with `CardView` for each item.
*   **Place Details View:** A dedicated screen (`HappyPlaceDetailActivity`) to show the image and details of a selected place.
*   **Swipe-to-Delete:** Easily remove entries from the list using a swipe gesture (`ItemTouchHelper`, `SwipeToDeleteCallback`).
*   **Permissions Handling:** Uses the Dexter library to request necessary permissions (Camera, Storage, Location).
*   **User Interface:**
    *   Uses standard Android UI components (`Toolbar`, `EditText`, `Button`, `ImageView`, `TextView`).
    *   Floating Action Button (FAB) for adding new places.
    *   Uses `CircleImageView` for displaying place images in the list.
    *   Includes a Lottie animation (`locationlottie.json`) for visual appeal, likely in the Add Place screen.

## Tech Stack

*   **Language:** Kotlin
*   **Platform:** Android
*   **UI Toolkit:** Android XML Layouts
    *   Views: `RecyclerView`, `CardView`, `Toolbar`, `FloatingActionButton`, `EditText`, `ImageView`, `TextView`, `CircleImageView` (de.hdodenhof.circleimageview)
    *   Material Components
*   **Database:** SQLite (via custom `SQLiteOpenHelper` - `DatabaseHandler`)
*   **Image Handling:** Android Camera Intent, Gallery Intent, Bitmap processing, Internal Storage saving.
*   **Location:** Google Places SDK for Android (Autocomplete)
*   **Permissions:** Dexter (`com.karumi:dexter`)
*   **Animations:** Lottie for Android (`com.airbnb.android:lottie`)
*   **Build System:** Gradle
*   **IDE:** Android Studio

## How It Works

1.  **Main Screen (`MainActivity`):** Displays the list of saved happy places fetched from the SQLite database using `DatabaseHandler`. If no places exist, it shows a "no records" message. Uses `HappyPlacesAdapter` to populate the `RecyclerView`.
2.  **Adding a Place (`AddHappyPlace`):**
    *   Accessed via the FAB on the main screen.
    *   Presents a form to input title, description, date (via `DatePickerDialog`), location, and image.
    *   Image selection uses Camera/Gallery intents, prompted by an `AlertDialog`. Permissions are handled by Dexter. The selected/captured image `Bitmap` is saved to internal storage, and its `Uri` is stored.
    *   Location input triggers the Google Places Autocomplete widget. The selected place's address and Lat/Lng are saved.
    *   On saving, a `HappyPlaceModel` object is created and passed to `DatabaseHandler` to insert into the SQLite table.
3.  **Viewing Details (`HappyPlaceDetailActivity`):**
    *   Launched when an item in the `RecyclerView` is clicked.
    *   Receives the `HappyPlaceModel` object via Intent extras.
    *   Displays the image (loaded via URI), description, and location.
4.  **Deleting a Place:**
    *   Implemented using `ItemTouchHelper` and the custom `SwipeToDeleteCallback` on the `RecyclerView` in `MainActivity`.
    *   Swiping an item left triggers the `onSwiped` method, which calls the adapter's `removeAt` function.
    *   The `removeAt` function uses `DatabaseHandler` to delete the corresponding entry from SQLite and then updates the RecyclerView.
5.  **Data Persistence:** All happy place details (including the image URI) are stored locally in an SQLite database managed by `DatabaseHandler`.

## Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd <repository-directory>
    ```
2.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Select "Open an Existing Project" and navigate to the cloned repository directory.
3.  **Configure API Keys:**
    *   **Google Maps API Key:** You need a Google Maps API key enabled for both the **Places SDK for Android** and the **Maps SDK for Android**.
    *   Obtain an API key from the [Google Cloud Console](https://console.cloud.google.com/google/maps-apis/overview).
    *   Add your key to **two** files:
        *   `app/src/debug/res/values/google_maps_api.xml` (for debug builds)
        *   `app/src/release/res/values/google_maps_api.xml` (for release builds)
    *   Replace `"YOUR_KEY_HERE"` with your actual API key in both files within the `<string name="google_maps_key">` tag.
4.  **(Optional) Firebase Setup:** The project includes a `google-services.json` file and Firebase dependencies, suggesting potential (or past) Firebase integration (possibly for cloud storage or database features not fully implemented in the provided code). If you intend to use Firebase features, ensure this file is correctly configured for your own Firebase project. Otherwise, it might not be strictly necessary for the core SQLite functionality shown.
5.  **Sync Gradle:**
    *   Allow Android Studio to sync the project with Gradle files. It will download necessary dependencies.

## Running the Application

1.  **Connect a Device or Start an Emulator:**
    *   Ensure you have an Android device connected via USB with developer options enabled, or an Android Virtual Device (Emulator) set up and running in Android Studio.
2.  **Run from Android Studio:**
    *   Select the target device/emulator from the dropdown menu.
    *   Click the "Run" button (green play icon) or use the menu `Run > Run 'app'`.

## Demo & Screenshots

*   **App Demo Video:** [https://youtu.be/fvbKGgqYFJc](https://youtu.be/fvbKGgqYFJc)
*   **Screenshots:** [https://drive.google.com/drive/folders/1rA6ya7cBqUxJ1zwg3tZuM_GPm1mwPbXl](https://drive.google.com/drive/folders/1rA6ya7cBqUxJ1zwg3tZuM_GPm1mwPbXl)
