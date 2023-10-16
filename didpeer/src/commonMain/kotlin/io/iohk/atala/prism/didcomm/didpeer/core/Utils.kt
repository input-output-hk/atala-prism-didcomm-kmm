package io.iohk.atala.prism.didcomm.didpeer.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
}

private fun extractFromJsonObject(jsonObject: JsonObject): Map<String, Any> {
    val currentMap = mutableMapOf<String, Any>()
    jsonObject.forEach {
        if (it.value is JsonPrimitive) {
            if (it.value.jsonPrimitive.isString) {
                currentMap[it.key] = it.value.jsonPrimitive.content
            } else {
                currentMap[it.key] = it.value
            }
        } else if (it.value is JsonArray) {
            val localArray = mutableListOf<String>()
            for (arrayJsonElement in it.value.jsonArray) {
                if (arrayJsonElement.jsonPrimitive.isString) {
                    localArray.add(arrayJsonElement.jsonPrimitive.content)
                } else {
                    localArray.add(arrayJsonElement.jsonPrimitive.toString())
                }
            }
            currentMap[it.key] = localArray
        } else {
            throw Exception("")
        }
    }
    return currentMap
}

/**
 * I'm expecting the value to be a JSON array
 */
fun fromJsonToList(value: String): List<Map<String, Any>> {
    val list: MutableList<Map<String, Any>> = mutableListOf()
    val element = Json.parseToJsonElement(value)

    if (element is JsonArray) {
        for (jsonElement in element.jsonArray) {
            list.add(extractFromJsonObject(jsonElement.jsonObject))
        }
    } else if (element is JsonObject) {
        list.add(extractFromJsonObject(element.jsonObject))
    } else {
        throw Exception("")
    }
    return list
}

fun fromJsonToMap(value: String): Map<String, Any> {
    val element = Json.parseToJsonElement(value)

    if (element is JsonObject) {
        return extractFromJsonObject(element)
    } else {
        throw Exception("")
    }
}
