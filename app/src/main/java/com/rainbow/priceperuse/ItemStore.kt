package com.rainbow.priceperuse

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ItemStore(context: Context) {
    private val prefs = context.getSharedPreferences("rainbow_inventory", Context.MODE_PRIVATE)
    var language: String
        get() = prefs.getString("language", "") ?: ""
        set(value) { prefs.edit().putString("language", value).apply() }
    var theme: Int
        get() = prefs.getInt("theme", 0).coerceIn(0, AppPalette.all.lastIndex)
        set(value) { prefs.edit().putInt("theme", value.coerceIn(0, AppPalette.all.lastIndex)).apply() }
    var onboardingSeen: Boolean
        get() = prefs.getBoolean("onboarding_seen", false)
        set(value) { prefs.edit().putBoolean("onboarding_seen", value).apply() }
    fun load(): MutableList<Item> {
        val array = try { JSONArray(prefs.getString("items", "[]")) } catch (_: Exception) { JSONArray() }
        return (0 until array.length()).mapNotNull { index ->
            try { array.getJSONObject(index).let { Item(it.getString("id"), it.getString("name"), it.getLong("cents"), it.getInt("icon").coerceIn(0,ItemIcons.count-1), it.getInt("uses").coerceAtLeast(0)) } } catch (_: Exception) { null }
        }.toMutableList()
    }
    fun save(items: List<Item>) {
        val array = JSONArray()
        items.forEach { array.put(JSONObject().put("id", it.id).put("name", it.name).put("cents", it.cents).put("icon", it.icon).put("uses", it.uses)) }
        prefs.edit().putString("items", array.toString()).apply()
    }
}
