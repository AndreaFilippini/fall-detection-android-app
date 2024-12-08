package com.example.falldetection.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R

class PhoneAdapter(
    private val onDelete: (String) -> Unit) : RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder>() {

    private var phoneNumbers = listOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_phone_number, parent, false)
        return PhoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
        val phoneNumber = phoneNumbers[position]
        holder.phoneTextView.text = phoneNumber
        holder.deleteButton.setOnClickListener {
            onDelete(phoneNumber) // Trigger delete callback when button is clicked
        }
    }

    override fun getItemCount(): Int = phoneNumbers.size

    fun setPhoneNumbers(phoneNumbers: List<String>) {
        this.phoneNumbers = phoneNumbers
        notifyDataSetChanged()
    }

    class PhoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }
}
