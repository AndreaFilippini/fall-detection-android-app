package com.example.falldetection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.falldetection.ui.notifications.Notification

class SharedNotificationModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesHelper = SharedPreferencesHelper(application)

    private val _notifications = MutableLiveData<MutableList<Notification>>().apply {
        value = sharedPreferencesHelper.loadNotifications()
    }
    val notifications: LiveData<MutableList<Notification>> = _notifications

    fun addNotification(notification: Notification) {
        val updatedNotifications = _notifications.value ?: mutableListOf()
        updatedNotifications.add(0, notification)
        _notifications.value = updatedNotifications
        sharedPreferencesHelper.saveNotifications(updatedNotifications)
    }

    fun deleteNotifications() {
        _notifications.value = mutableListOf()
        sharedPreferencesHelper.deleteNotifications()
    }
}