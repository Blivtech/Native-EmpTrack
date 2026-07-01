package com.blivtech.emptrack.utils

object CommonClass {
    fun parseErrorMessage(errorBody: String?): String? {
        return try {
            val json = org.json.JSONObject(errorBody ?: return null)
            json.optString("message").ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
}