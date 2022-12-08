package io.iohk.atala.prism.mercury.didpeer.core

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Array<*> -> this.toJsonArray()
        is List<*> -> this.toJsonArray()
        is Map<*, *> -> this.toJsonObject()
        is JsonElement -> this
        else -> JsonNull
    }
}

fun Array<*>.toJsonArray() = JsonArray(map { it.toJsonElement() })
fun Iterable<*>.toJsonArray() = JsonArray(map { it.toJsonElement() })
fun Map<*, *>.toJsonObject() = JsonObject(mapKeys { it.key.toString() }.mapValues { it.value.toJsonElement() })

fun Json.encodeToString(vararg pairs: Pair<*, *>) = encodeToString(pairs.toMap().toJsonElement())

fun toJson(value: Any?): String {
    return Json.encodeToString(value.toJsonElement())
    // GsonBuilder().create().toJson(value)
}

fun fromJsonToList(value: String): List<Map<String, Any>> {
    return Json.decodeFromString<List<Map<String, Any>>>(value)
    // return GsonBuilder().create().fromJson(value, object : TypeToken<List<Map<String, Any>>>() {}.type)
}

fun fromJsonToMap(value: String): Map<String, Any> {
    return Json.decodeFromString<Map<String, Any>>(value)
    // return GsonBuilder().create().fromJson(value, object : TypeToken<Map<String, Any>>() {}.type)
}
