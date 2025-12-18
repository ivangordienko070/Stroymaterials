// data/preferences/AppPreferences.kt
package com.example.stroymaterials.data.preferences

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )

    var lowStockNotifications: Boolean
        get() = prefs.getBoolean("low_stock_notifications", true)
        set(value) = prefs.edit().putBoolean("low_stock_notifications", value).apply()

    var criticalNotifications: Boolean
        get() = prefs.getBoolean("critical_notifications", true)
        set(value) = prefs.edit().putBoolean("critical_notifications", value).apply()

    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system"
        set(value) = prefs.edit().putString("theme_mode", value).apply()
}

