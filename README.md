# The Hive 🏠

**The Hive** is a modern, native Android application designed to bridge the gap between landlords and house hunters. It provides a seamless platform for browsing property listings, saving favourites, managing property listings, contacting landlords through multiple communication channels, and keeping property availability up-to-date with automated background reminders.

---

## 🏗️ Architecture & Technical Stack

The Hive is built following Google's recommended Android architecture practices (MVVM + Repository Pattern) to ensure maintainability, scalability, and testability.

* **Language**: Kotlin 1.9+
* **Architecture**: Model-View-ViewModel (MVVM) + Repository Pattern
* **UI & Navigation**: Single-Activity Architecture (`MainActivity`), AndroidX Navigation Component with NavHost, ViewBinding, and Material Design 3 UI components.
* **Database & Persistence**: Room Persistence Library with TypeConverters, Foreign Keys, Indexing, and Flow-based reactive queries.
* **Background Processing**: WorkManager (`PeriodicWorkRequest`, `OneTimeWorkRequest`) for automated landlord availability reminders.
* **Notifications**: Notification Channels, Android 13+ `POST_NOTIFICATIONS` permission handling, and `BroadcastReceiver` (`ReminderActionReceiver`) for inline notification action handlers.
* **Asynchronous Execution**: Kotlin Coroutines & `StateFlow` / `SharedFlow` / `Flow` for reactive UI updates.

---

## 🚀 Setup & Installation Guide

### Prerequisites
* **Android Studio**: Android Studio Ladybug (2024.2.1) or higher.
* **JDK**: OpenJDK 17.
* **Min SDK**: API 26 (Android 8.0 Oreo).
* **Target SDK**: API 37 (Android 15+).

### Building and Running
1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/the-hive.git
   cd the-hive
   ```
2. Open the project in Android Studio.
3. Sync the Gradle files (`File -> Sync Project with Gradle Files`).
4. Select your target Android device or emulator (API 26 or higher).
5. Build and run the app (`Shift + F10` or click **Run 'app'**).

---

## 💡 Key Features

* **User Authentication & Role Management**:
  * Register and Log in as either a **House Hunter** or a **Landlord**.
  * Role-tailored UI navigation: Landlords get access to "My Listings" management, while House Hunters focus on browsing and saving favourites.

* **Browse & Filter Listings**:
  * Real-time search by location name.
  * Filter by minimum/maximum rent price and bedroom count.
  * Toggle options to show or hide listings marked as "Taken".

* **Multi-Channel Contact System**:
  * Prefilled message builder (`EnquiryMessageBuilder`) containing listing details, location, and sender's full name.
  * `ContactBottomSheet` offering Email (`mailto:`), SMS (`smsto:`), and WhatsApp (`wa.me`) options.
  * Safe intent launching with friendly fallback error handling when no compatible app is installed.

* **Landlord Listing Management**:
  * Add, edit, or delete property listings.
  * Toggle listing availability status ("Available" vs. "Taken").
  * Automatic duplicate listing detection based on title and location.

* **Automated Availability Reminders (WorkManager)**:
  * `AvailabilityReminderWorker` periodically checks if a landlord's listings have not been confirmed within $N$ days.
  * Triggers interactive push notifications with direct action buttons ("Still available" and "Mark taken").
  * Direct database updates from notification actions via `ReminderActionReceiver`.

* **Settings & Preferences**:
  * Toggle availability reminders ON or OFF.
  * Customize reminder check frequency (Every 3, 7, 14, or 30 days).
  * Direct handling and status monitoring for `POST_NOTIFICATIONS` permission on Android 13+.
  * "Test Reminder Check Now" manual trigger for debugging and instant verification.

---

## 👥 Team Contributions Table

| Member | Focus Area | Key Deliverables & Implemented Tasks |
| :--- | :--- | :--- |
| **Member 1** | Database & Models | Room Database setup (`AppDatabase`), Entities (`UserEntity`, `ListingEntity`, `FavouriteEntity`), Data Access Objects (`UserDao`, `ListingDao`, `FavouriteDao`), Type Converters, and Repository interfaces/implementations. |
| **Member 2** | Authentication & Profile | User registration and login flow (`LoginFragment`, `RegisterFragment`, `AuthViewModel`), password hashing (`PasswordHasher`), session persistence (`SessionManager`), user profile management (`ProfileFragment`, `ProfileViewModel`). |
| **Member 3** | Browse & Search | Main browsing feed (`HomeFragment`, `BrowseViewModel`), filter dialog (`dialog_filter.xml`), search query handling, and listing item adapter (`ListingsAdapter`). |
| **Member 4** | Detail & Favourites | Listing details view (`DetailFragment`, `DetailViewModel`), favourite toggling & storage (`FavouritesFragment`, `FavouritesViewModel`, `FavouritesAdapter`), integration with landlord contact options. |
| **Member 5** | My Listings Management | Landlord listing dashboard (`MyListingsFragment`, `MyListingsViewModel`), listing creation and editing form (`AddEditListingFragment`, `ListingFormViewModel`), availability toggling, and duplicate listing validation (`ListingValidator`). |
| **Member 6** | Contact, WorkManager, Settings & Deliverables | • **EnquiryMessageBuilder**: Prefilled enquiry text incorporating listing title, location, and sender's name.<br>• **ContactHelper**: Multi-channel intent dispatching for Email (`ACTION_SENDTO mailto:`), SMS (`smsto:`), and WhatsApp (`https://wa.me/`), with graceful fallback error handling.<br>• **ContactBottomSheet**: Material bottom sheet dialog allowing users to choose Email, SMS, or WhatsApp on the Detail screen.<br>• **AvailabilityReminderWorker**: `PeriodicWorkRequest` running background availability checks on landlord listings older than $N$ days.<br>• **NotificationHelper & ReminderActionReceiver**: Notification channel creation, Android 13+ `POST_NOTIFICATIONS` support, and inline notification action handling ("Still available", "Mark taken").<br>• **ReminderScheduler**: Periodic work enqueuing and cancellation on logout or feature toggle.<br>• **SettingsScreen & SettingsViewModel**: Full settings screen for configuring reminder toggles, frequency, and test checks.<br>• **README Documentation**: Comprehensive documentation of project architecture, setup, features, and team contributions table. |

---

## 🧪 Testing

Run unit tests via Gradle:
```bash
./gradlew testDebugUnitTest
```
Or run instrumentation tests on a connected device/emulator:
```bash
./gradlew connectedAndroidTest
```
