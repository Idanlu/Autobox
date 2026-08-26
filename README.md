# Autobox: Arbox Auto-Scheduler Native Android App

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="96" height="96" alt="Autobox Logo" />
</p>

**Autobox** is a high-performance native Android application designed to automatically snipe and book gym sessions on the Arbox platform at the millisecond booking windows open.

The app bypasses external server dependencies by running schedule synchronization, session tracking, token management, and high-precision API bursts directly on the user's Android device using `WorkManager`, exact `AlarmManager` alarms, and CPU `WakeLock` management.

---

## 1. System Architecture

```
[ Compose UI ] ──(User Credentials & Rules)──> [ EncryptedSharedPreferences ]
                                                            │
                                                            ▼
                                                   [ ScheduleSyncWorker ]
                                                            │
                                        (Sets Exact Time via AlarmManager)
                                                            │
                                                            ▼
                                                [ ClassBookingReceiver ]
                                                            │
                                                            ▼
                                                   [ ArboxApiService ]
                                                            │
                                                  (HTTPS REST Requests)
                                                            │
                                                            ▼
                                                   [ Arbox Backend API ]
```

---

## 2. The Precision "Snipe" Strategy

Due to aggressive Android battery management (Doze Mode), Autobox splits tasks into two execution tiers:

1. **Schedule Pre-fetch (`WorkManager`)**
   - Class: `ScheduleSyncWorker` (extends `CoroutineWorker`)
   - Runs periodically (every 12 hours) and on-demand to fetch gym schedules and map target session IDs and opening timestamp $T$.

2. **Precision Booking Fire (`AlarmManager` + `BroadcastReceiver` + `WakeLock`)**
   - Class: `ClassBookingReceiver` (extends `BroadcastReceiver`)
   - Uses `AlarmManager.setExactAndAllowWhileIdle()` scheduled to fire at $T - 5\text{s}$.

```
T - 5s : AlarmManager wakes CPU via BroadcastReceiver & acquires temporary PowerManager.WakeLock
T - 3s : Fetch fresh session state to confirm session_id availability
T - 0s : Fire concurrent parallel POST booking requests via OkHttp HTTP/2 connection pool
T + 1s : Parse HTTP response & post heads-up notification (Success / Waitlisted / Failed)
```

---

## 3. Arbox API Endpoints Specification

| Phase | Endpoint | Method | Key Headers / Payload |
| --- | --- | --- | --- |
| **Login** | `https://api.arboxapp.com/api/v2/user/login` | `POST` | `{"email": "...", "password": "..."}` |
| **Fetch Schedule** | `https://api.arboxapp.com/api/v2/schedule` | `GET` | Header: `Authorization: Bearer <token>`<br>Params: `box_id`, `startDate`, `endDate` |
| **Session Info** | `https://api.arboxapp.com/api/v2/schedule/session/{sessionId}` | `GET` | Header: `Authorization: Bearer <token>` |
| **Book Session** | `https://api.arboxapp.com/api/v2/user/memberships/{membership_id}/book` | `POST` | Header: `Authorization: Bearer <token>`<br>Payload: `{"session_id": "<ID>", "standby": false}` |

---

## 4. Key Configuration & Android Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

> **Important Battery Note:** To ensure exact alarm execution when the device is asleep in deep Doze mode, users should whitelist Autobox using the in-app **"Disable Battery Optimization"** prompt (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).

---

## 5. Project Structure

```
Autobox/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/autobox/app/
│   │   │   │   ├── AutoboxApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── background/
│   │   │   │   │   ├── AlarmScheduler.kt
│   │   │   │   │   ├── BootReceiver.kt
│   │   │   │   │   ├── ClassBookingReceiver.kt
│   │   │   │   │   ├── NotificationHelper.kt
│   │   │   │   │   └── ScheduleSyncWorker.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── ArboxApiService.kt
│   │   │   │   │   │   └── NetworkModule.kt
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── EncryptedPreferencesManager.kt
│   │   │   │   │   │   ├── RulesRepository.kt
│   │   │   │   │   │   └── SnipeLogsRepository.kt
│   │   │   │   │   ├── models/
│   │   │   │   │   │   └── ArboxModels.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── ArboxAuthRepository.kt
│   │   │   │   │       ├── ArboxBookingRepository.kt
│   │   │   │   │       └── ArboxScheduleRepository.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   └── CommonUi.kt
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── AuthScreen.kt
│   │   │   │   │   │   ├── LogsScreen.kt
│   │   │   │   │   │   ├── MainScreen.kt
│   │   │   │   │   │   ├── RulesScreen.kt
│   │   │   │   │   │   ├── ScheduleScreen.kt
│   │   │   │   │   │   └── SettingsScreen.kt
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   └── viewmodels/
│   │   │   │   │       ├── AuthViewModel.kt
│   │   │   │   │       ├── RulesViewModel.kt
│   │   │   │   │       ├── ScheduleViewModel.kt
│   │   │   │   │       └── SettingsViewModel.kt
│   │   │   │   └── util/
│   │   │   │       ├── BatteryOptimizationHelper.kt
│   │   │   │       └── DateTimeUtils.kt
│   │   │   └── res/
│   │   └── test/java/com/autobox/app/
│   │       ├── BookingRuleMatchingTest.kt
│   │       ├── DateTimeUtilsTest.kt
│   │       └── SnipeBurstStrategyTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 6. Build and Run

### Prerequisites
- Android Studio Ladybug (2024.2+) or Android CLI
- JDK 17+
- Android SDK 35 (minSdk 26)

### Build Commands
```bash
# Clean and build debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew test
```
