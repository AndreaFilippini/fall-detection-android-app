package com.example.falldetection
import android.content.Context
import com.example.falldetection.ui.dashboard.ListItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.falldetection.ui.notifications.Notification

class SharedPreferencesHelper(context: Context) {
    private val sharedIpPreferences = context.getSharedPreferences("ip_prefs", Context.MODE_PRIVATE)
    private val sharedSoundPreferences = context.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE)
    private val sharedEmailsPreferences = context.getSharedPreferences("emails_prefs", Context.MODE_PRIVATE)
    private val sharedPhoneNumbersPreferences = context.getSharedPreferences("numbers_prefs", Context.MODE_PRIVATE)
    private val sharedListPreferences = context.getSharedPreferences("list_prefs", Context.MODE_PRIVATE)
    private val sharedTimerPreferences = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
    private val sharedDelayPreferences = context.getSharedPreferences("delay_prefs", Context.MODE_PRIVATE)
    private val sharedNotificationPreferences = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun setIp(list: Array<Int>) {
        val jsonString = gson.toJson(list)
        sharedIpPreferences.edit().putString("ip", jsonString).apply()
        //sharedIpPreferences.edit().remove("ip").apply()
    }

    fun getIp(): Array<Int> {
        val jsonString = sharedIpPreferences.getString("ip", null)
        val type = object : TypeToken<Array<Int>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyArray<Int>()
    }

    fun saveEmails(list: List<String>) {
        val jsonString = gson.toJson(list)
        sharedEmailsPreferences.edit().putString("emails", jsonString).apply()
        //sharedEmailsPreferences.edit().remove("emails").apply()
    }

    fun loadEmails(): List<String> {
        val jsonString = sharedEmailsPreferences.getString("emails", null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(jsonString, type) ?: mutableListOf()
    }

    fun setSoundTimer(seconds: Int) {
        val jsonString = gson.toJson(seconds)
        sharedSoundPreferences.edit().putString("sound", jsonString).apply()
        //sharedTimerPreferences.edit().remove("seconds").apply()
    }

    fun getSoundTimer() : Int? {
        val jsonString = sharedSoundPreferences.getString("sound", null) ?: return null
        val type = object : TypeToken<Int>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun savePhoneNumbers(list: List<String>) {
        val jsonString = gson.toJson(list)
        sharedPhoneNumbersPreferences.edit().putString("numbers", jsonString).apply()
        //sharedPhoneNumbersPreferences.edit().remove("numbers").apply()
    }

    fun loadPhoneNumbers(): List<String> {
        val jsonString = sharedPhoneNumbersPreferences.getString("numbers", null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(jsonString, type) ?: mutableListOf()
    }

    fun savePriorityList(list: List<ListItem>) {
        val jsonString = gson.toJson(list)
        sharedListPreferences.edit().putString("priority", jsonString).apply()
        //sharedListPreferences.edit().remove("priority").apply()
    }

    fun loadPriorityList(): MutableList<ListItem>? {
        val jsonString = sharedListPreferences.getString("priority", null) ?: return null
        val type = object : TypeToken<MutableList<ListItem>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun setTimer(seconds: Int) {
        val jsonString = gson.toJson(seconds)
        sharedTimerPreferences.edit().putString("seconds", jsonString).apply()
        //sharedTimerPreferences.edit().remove("seconds").apply()
    }

    fun getTimer() : Int? {
        val jsonString = sharedTimerPreferences.getString("seconds", null) ?: return null
        val type = object : TypeToken<Int>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun setDelay(seconds: Int) {
        val jsonString = gson.toJson(seconds)
        sharedDelayPreferences.edit().putString("seconds", jsonString).apply()
        //sharedDelayPreferences.edit().remove("seconds").apply()
    }

    fun getDelay() : Int? {
        val jsonString = sharedDelayPreferences.getString("seconds", null) ?: return null
        val type = object : TypeToken<Int>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun saveNotifications(notifications: List<Notification>) {
        val jsonString = gson.toJson(notifications)
        sharedNotificationPreferences.edit().putString("notifications", jsonString).apply()
    }

    fun deleteNotifications() {
        sharedNotificationPreferences.edit().remove("notifications").apply()
    }

    fun loadNotifications(): MutableList<Notification> {
        val jsonString = sharedNotificationPreferences.getString("notifications", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Notification>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}