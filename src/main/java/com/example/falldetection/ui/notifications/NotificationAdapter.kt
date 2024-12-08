package com.example.falldetection.ui.notifications

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R
import java.util.Date

class NotificationAdapter(private var notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idTextView: TextView = view.findViewById(R.id.notificationId)
        val timeTextView: TextView = view.findViewById(R.id.notificationTime)
        val locationTextView: TextView = view.findViewById(R.id.notificationLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.idTextView.text = "Notification ${notification.id}"
        holder.timeTextView.text = "Time: ${Date(notification.timestamp)}"
        holder.locationTextView.text = "Location: ${notification.location}"
    }

    fun updateData(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = notifications.size
}