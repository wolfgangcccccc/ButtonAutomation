package com.buttonautomation.domain.model

import java.util.UUID

data class MacroButton(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "⚡",
    val colorHex: String = "#6200EE",
    val actions: List<Action> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

sealed class Action {
    abstract val id: String
    abstract val label: String

    data class OpenApp(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "App öffnen",
        val packageName: String = ""
    ) : Action()

    data class OpenUrl(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "URL öffnen",
        val url: String = ""
    ) : Action()

    data class WebSearch(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "Websuche",
        val query: String = ""
    ) : Action()

    data class OpenSettings(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "Einstellungen öffnen",
        val settingsType: SettingsType = SettingsType.GENERAL
    ) : Action()

    data class Delay(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "Pause",
        val milliseconds: Long = 1000L
    ) : Action()

    data class OpenFile(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "Datei/Galerie öffnen",
        val mimeType: String = "*/*"
    ) : Action()

    data class SendIntent(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "Intent senden",
        val action: String = "",
        val data: String = "",
        val extras: Map<String, String> = emptyMap()
    ) : Action()

    data class ShareText(
        override val id: String = UUID.randomUUID().toString(),
        override val label: String = "Text teilen",
        val text: String = ""
    ) : Action()
}

enum class SettingsType(val displayName: String, val androidAction: String) {
    GENERAL("Allgemein", "android.settings.SETTINGS"),
    BLUETOOTH("Bluetooth", "android.settings.BLUETOOTH_SETTINGS"),
    WIFI("WLAN", "android.settings.WIFI_SETTINGS"),
    SOUND("Ton & Vibration", "android.settings.SOUND_SETTINGS"),
    DISPLAY("Display", "android.settings.DISPLAY_SETTINGS"),
    LOCATION("Standort", "android.settings.LOCATION_SOURCE_SETTINGS"),
    APPS("Apps", "android.settings.APPLICATION_SETTINGS"),
    BATTERY("Akku", "android.settings.BATTERY_SAVER_SETTINGS"),
    NOTIFICATIONS("Benachrichtigungen", "android.settings.APP_NOTIFICATION_SETTINGS"),
    AIRPLANE("Flugmodus", "android.settings.AIRPLANE_MODE_SETTINGS"),
    NFC("NFC", "android.settings.NFC_SETTINGS"),
    HOTSPOT("Hotspot", "android.settings.WIRELESS_SETTINGS"),
    DATE_TIME("Datum & Uhrzeit", "android.settings.DATE_SETTINGS"),
    ACCESSIBILITY("Barrierefreiheit", "android.settings.ACCESSIBILITY_SETTINGS"),
    DEVELOPER("Entwickleroptionen", "android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
}

enum class ActionType(val displayName: String, val emoji: String) {
    OPEN_APP("App öffnen", "📱"),
    OPEN_URL("URL öffnen", "🌐"),
    WEB_SEARCH("Websuche", "🔍"),
    OPEN_SETTINGS("Einstellungen", "⚙️"),
    DELAY("Pause / Delay", "⏱️"),
    OPEN_FILE("Datei / Galerie", "📁"),
    SEND_INTENT("Intent senden", "📡"),
    SHARE_TEXT("Text teilen", "📤")
}
