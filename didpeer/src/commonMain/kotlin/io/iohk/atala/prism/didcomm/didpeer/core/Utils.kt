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

/**
 * Converts an object to its JSON representation as a [JsonElement].
 *
 * @return The JSON representation of the object as a [JsonElement].
 */
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

/**
 * Converts an array to a JSON array.
 *
 * @return The JSON array representation of the array.
 */
fun Array<*>.toJsonArray() = JsonArray(map { it.toJsonElement() })

/**
 * Converts an Iterable of any type to a JSON array.
 *
 * @return The JSON array representation of the Iterable.
 */
fun Iterable<*>.toJsonArray() = JsonArray(map { it.toJsonElement() })

/**
 * Converts the given [Map] object to a [JsonObject].
 * Each key-value pair in the map is converted to a corresponding key-value pair in the JsonObject.
 * The keys are converted to string representation using the [toString] method.
 * The values are converted to [JsonElement] using the [toJsonElement] method.
 *
 * @return the converted JsonObject.
 *
 * @see toJsonElement
 */
fun Map<*, *>.toJsonObject() = JsonObject(mapKeys { it.key.toString() }.mapValues { it.value.toJsonElement() })

/**
 * Encodes the given pairs of key-value data into a JSON string representation.
 *
 * @param pairs the key-value pairs to encode
 * @return the JSON string representation of the encoded data
 */
fun Json.encodeToString(vararg pairs: Pair<*, *>) = encodeToString(pairs.toMap().toJsonElement())

/**
 * Converts the given [value] to its JSON representation as a string.
 *
 * @param value the value to convert to JSON
 * @return the JSON representation of the value as a string
 */
fun toJson(value: Any?): String {
    return Json.encodeToString(value.toJsonElement())
}

/**
 * Extracts the key-value pairs from a given JsonObject and returns them as a Map.
 *
 * @param jsonObject the JsonObject from which to extract key-value pairs
 * @return a Map containing the extracted key-value pairs
 * @throws Exception if the value of any key in the JsonObject is not of type JsonPrimitive or JsonArray
 */
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
 * Converts a JSON string to a List of Maps.
 *
 * @param value The JSON string to convert.
 * @return The converted List of Maps.
 * @throws Exception if the JSON string is not valid.
 */
fun fromJsonToList(value: String): List<Map<String, Any>> {
    val list: MutableList<Map<String, Any>> = mutableListOf()
    when (val element = Json.parseToJsonElement(value)) {
        is JsonArray -> {
            for (jsonElement in element.jsonArray) {
                list.add(extractFromJsonObject(jsonElement.jsonObject))
            }
        }

        is JsonObject -> {
            list.add(extractFromJsonObject(element.jsonObject))
        }

        else -> {
            throw Exception("")
        }
    }
    return list
}

/**
 * Converts a JSON string to a Map<String, Any> object.
 *
 * @param value The JSON string to convert.
 * @return The converted Map<String, Any> object.
 * @throws Exception if the JSON string is not valid or if the value of any key in the JSON object is not of type JsonPrimitive or JsonArray.
 */
fun fromJsonToMap(value: String): Map<String, Any> {
    val element = Json.parseToJsonElement(value)

    if (element is JsonObject) {
        return extractFromJsonObject(element)
    } else {
        throw Exception("")
    }
}
