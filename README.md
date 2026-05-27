# ⚡ Button Automation App

Eine Android-App zum Erstellen eigener Automatisierungs-Buttons mit frei kombinierbaren Aktionsketten.

## Projektstruktur

```
app/src/main/java/com/buttonautomation/
├── ButtonAutomationApp.kt          ← Hilt Application
├── MainActivity.kt
├── data/
│   ├── database/
│   │   ├── ButtonAutomationDatabase.kt
│   │   ├── MacroButtonDao.kt
│   │   └── MacroButtonEntity.kt    ← Entity + Gson Converter + Mapper
│   └── repository/
│       └── MacroButtonRepository.kt
├── di/
│   └── AppModule.kt                ← Hilt Module
├── domain/
│   ├── model/
│   │   └── Models.kt               ← MacroButton, Action (sealed), ActionType, SettingsType
│   └── usecase/
│       ├── ActionEngine.kt         ← ⭐ Kern-Engine: führt alle 8 Actions aus
│       └── UseCases.kt
└── presentation/
    ├── Navigation.kt
    ├── Theme.kt
    ├── components/
    │   └── ActionEditorDialogs.kt  ← Dialog für jeden Action-Typ
    ├── screens/
    │   ├── HomeScreen.kt           ← Button-Liste + Execute
    │   └── EditorScreen.kt         ← Button erstellen/bearbeiten
    └── viewmodel/
        ├── HomeViewModel.kt
        └── EditorViewModel.kt
```

## Setup in Android Studio

1. **Projekt öffnen**: `File → Open → ButtonAutomation/`
2. **Gradle Sync** abwarten
3. **Run** auf echtem Gerät oder Emulator (API 26+)

## Unterstützte Aktionen

| Aktion | Beschreibung |
|--------|-------------|
| 📱 App öffnen | Via Package Name oder App-Picker |
| 🌐 URL öffnen | Beliebige URL im Browser |
| 🔍 Websuche | Google-Suche starten |
| ⚙️ Einstellungen | Bluetooth, WLAN, Display, etc. (15 Typen) |
| ⏱️ Pause/Delay | Wartezeit zwischen Aktionen |
| 📁 Datei/Galerie | Bilder, Videos, PDF öffnen |
| 📡 Intent senden | Benutzerdefinierter Android Intent |
| 📤 Text teilen | Share-Sheet öffnen |

## Beispiel-Buttons

**Music Mode:**
1. Einstellungen → Bluetooth
2. Pause: 1s  
3. App öffnen: com.spotify.music

**Browse Mode:**
1. App öffnen: com.opera.browser
2. URL öffnen: https://google.com

**Fun Mode:**
1. App öffnen: com.google.android.youtube
2. Websuche: funny cats

## Tech Stack

- Kotlin + Jetpack Compose
- Room Database
- Hilt (Dependency Injection)
- MVVM + Clean Architecture
- Intent-basierte Automation
