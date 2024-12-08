package com.example.falldetection.ui.dashboard
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R

class EmailAdapter(
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<EmailAdapter.EmailViewHolder>() {

    private var emailList = listOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_email, parent, false)
        return EmailViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val emailString = emailList[position]
        holder.emailTextView.text = emailString
        holder.deleteButton.setOnClickListener {
            onDelete(emailString) // Trigger delete callback when button is clicked
        }
    }

    override fun getItemCount(): Int = emailList.size

    fun setEmail(emailList: List<String>) {
        this.emailList = emailList
        notifyDataSetChanged()
    }

    class EmailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }
}