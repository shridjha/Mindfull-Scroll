# MindfulScroll - Anti-Doomscrolling App

An Android application that helps users break free from doomscrolling habits by intelligently blocking short-form video content (Reels, Shorts, TikTok) and implementing cognitive barriers to encourage mindful social media usage.

## Features

### ✅ Core Features Implemented

1. **Short-Form Video Blocker**
   - Accessibility Service that detects when users navigate to Reels, Shorts, or TikTok feeds
   - Automatically triggers back button to exit short-form content
   - Configurable per app

2. **App Usage Timer**
   - Tracks usage time for monitored apps using UsageStatsManager
   - Daily time limits per app (configurable 5-120 minutes)
   - Shows blocking screen when time limit is reached

3. **Math Challenge System**
   - Cognitive friction through math problems
   - Three difficulty levels: Easy, Medium, Hard
   - Grants additional time (5-15 minutes) per successful challenge
   - Limited challenges per day (default: 3)

4. **Dashboard UI**
   - Today's usage summary
   - Monitored apps with usage progress bars
   - Block statistics
   - Settings navigation

5. **Database & Data Storage**
   - Room database for usage history
   - App settings storage
   - Challenge history tracking
   - All data stored locally (privacy-first)

## Technical Architecture

### Technology Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM
- **Database**: Room
- **Dependency Injection**: Manual (ViewModelFactory)
- **Background Services**: Foreground Service for monitoring

### Key Components

1. **Services**
   - `MindfulScrollAccessibilityService`: Detects and blocks Reels/Shorts
   - `AppUsageMonitorService`: Tracks app usage and enforces limits

2. **Database Entities**
   - `AppUsage`: Daily usage tracking
   - `AppSettings`: Per-app configuration
   - `ChallengeHistory`: Math challenge records

3. **UI Screens**
   - `DashboardScreen`: Main screen with usage overview
   - `SettingsScreen`: App configuration
   - `BlockingOverlayActivity`: Full-screen blocking interface
   - `MathChallengeActivity`: Challenge interface

## Setup Instructions

### Required Permissions

The app requires several permissions that must be granted manually:

1. **Usage Stats Permission**
   - Go to Settings → Apps → Special app access → Usage access
   - Enable for MindfulScroll

2. **Accessibility Service**
   - Go to Settings → Accessibility → Downloaded apps
   - Enable "MindfulScroll Accessibility Service"

3. **Display Over Other Apps**
   - Go to Settings → Apps → Special app access → Display over other apps
   - Enable for MindfulScroll

### Building the Project

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on a device (API 26+)

**Note**: This app requires physical device testing. Accessibility services and usage stats don't work properly on emulators.

## Project Structure

```
app/src/main/java/com/example/enddoomscroll/
├── data/
│   ├── dao/           # Room DAOs
│   ├── database/      # Database configuration
│   ├── entity/        # Database entities
│   └── repository/    # Data repository
├── service/           # Background services
├── ui/
│   ├── blocking/      # Blocking overlay UI
│   ├── challenge/     # Math challenge UI
│   ├── dashboard/     # Main dashboard
│   ├── settings/      # Settings screen
│   ├── theme/         # UI theming
│   └── viewmodel/     # ViewModels
├── util/              # Utility classes
└── receiver/          # Broadcast receivers
```

## Configuration

### Default Settings

- Default time limit: 30 minutes per app
- Math challenge difficulty: Medium
- Challenge reward: 10 minutes
- Max challenges per day: 3

These can be configured per app in the Settings screen.

## Known Limitations

1. **Detection Reliability**: Reels/Shorts detection depends on UI element identifiers that may change with app updates
2. **Battery Impact**: Continuous monitoring may impact battery life
3. **Root Access**: Some blocking features may require root access for complete functionality
4. **App Updates**: Social media apps update frequently, which may require updating detection logic

## Future Enhancements

- Custom blocking schedules (e.g., block during work hours)
- More challenge types (puzzles, memory games)
- Weekly/monthly usage reports
- Integration with Digital Wellbeing API
- App usage pattern analysis

## Privacy

- All data stored locally on device
- No cloud sync
- No analytics or tracking
- No account required

## License

[Specify your license here]

## Contributing

[Add contributing guidelines if needed]

