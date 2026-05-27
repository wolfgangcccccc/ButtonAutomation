package com.buttonautomation.data.database

import androidx.room.*
import com.buttonautomation.domain.model.Action
import com.buttonautomation.domain.model.MacroButton
import com.buttonautomation.domain.model.SettingsType
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

// ─── Entity ──────────────────────────────────────────────────────────────────

@Entity(tableName = "macro_buttons")
data class MacroButtonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val actionsJson: String,
    val createdAt: Long
)

// ─── Type Converters ──────────────────────────────────────────────────────────

class ActionTypeConverter {

    private val gson: Gson = GsonBuilder()
        .registerTypeHierarchyAdapter(Action::class.java, ActionSerializer())
        .create()

    @TypeConverter
    fun fromActionList(actions: List<Action>): String = gson.toJson(actions)

    @TypeConverter
    fun toActionList(json: String): List<Action> {
        val type = object : TypeToken<List<Action>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class ActionSerializer : JsonSerializer<Action>, JsonDeserializer<Action> {

    override fun serialize(src: Action, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("type", src::class.simpleName)
        obj.addProperty("id", src.id)
        obj.addProperty("label", src.label)
        when (src) {
            is Action.OpenApp -> obj.addProperty("packageName", src.packageName)
            is Action.OpenUrl -> obj.addProperty("url", src.url)
            is Action.WebSearch -> obj.addProperty("query", src.query)
            is Action.OpenSettings -> obj.addProperty("settingsType", src.settingsType.name)
            is Action.Delay -> obj.addProperty("milliseconds", src.milliseconds)
            is Action.OpenFile -> obj.addProperty("mimeType", src.mimeType)
            is Action.SendIntent -> {
                obj.addProperty("action", src.action)
                obj.addProperty("data", src.data)
                obj.add("extras", context.serialize(src.extras))
            }
            is Action.ShareText -> obj.addProperty("text", src.text)
        }
        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Action {
        val obj = json.asJsonObject
        val type = obj.get("type")?.asString ?: ""
        val id = obj.get("id")?.asString ?: ""
        val label = obj.get("label")?.asString ?: ""

        return when (type) {
            "OpenApp" -> Action.OpenApp(id, label, obj.get("packageName")?.asString ?: "")
            "OpenUrl" -> Action.OpenUrl(id, label, obj.get("url")?.asString ?: "")
            "WebSearch" -> Action.WebSearch(id, label, obj.get("query")?.asString ?: "")
            "OpenSettings" -> {
                val st = try { SettingsType.valueOf(obj.get("settingsType")?.asString ?: "GENERAL") }
                         catch (e: Exception) { SettingsType.GENERAL }
                Action.OpenSettings(id, label, st)
            }
            "Delay" -> Action.Delay(id, label, obj.get("milliseconds")?.asLong ?: 1000L)
            "OpenFile" -> Action.OpenFile(id, label, obj.get("mimeType")?.asString ?: "*/*")
            "SendIntent" -> {
                val extras: Map<String, String> = try {
                    val extrasType = object : TypeToken<Map<String, String>>() {}.type
                    context.deserialize(obj.get("extras"), extrasType) ?: emptyMap()
                } catch (e: Exception) { emptyMap() }
                Action.SendIntent(id, label, obj.get("action")?.asString ?: "", obj.get("data")?.asString ?: "", extras)
            }
            "ShareText" -> Action.ShareText(id, label, obj.get("text")?.asString ?: "")
            else -> Action.OpenApp(id, label)
        }
    }
}

// ─── Mapper ──────────────────────────────────────────────────────────────────

private val converter = ActionTypeConverter()

fun MacroButtonEntity.toDomain(): MacroButton = MacroButton(
    id = id,
    name = name,
    emoji = emoji,
    colorHex = colorHex,
    actions = converter.toActionList(actionsJson),
    createdAt = createdAt
)

fun MacroButton.toEntity(): MacroButtonEntity = MacroButtonEntity(
    id = id,
    name = name,
    emoji = emoji,
    colorHex = colorHex,
    actionsJson = converter.fromActionList(actions),
    createdAt = createdAt
)
