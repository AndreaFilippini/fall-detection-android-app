package com.example.falldetection.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R
import com.example.falldetection.SharedNotificationModel

class NotificationsFragment : Fragment() {
    private val notificationViewModel: SharedNotificationModel by activityViewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    private lateinit var notificationBanner: LinearLayout
    private lateinit var historyContainer: LinearLayout
    private lateinit var deleteButton: Button

    override fun onCreateView(    inflater: LayoutInflater,
                                  container: ViewGroup?,
                                  savedInstanceState: Bundle?
    ): View {

        // init fragment view UI based on the corresponding XML file
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        // initialize RecyclerView, getting the corresponding element from XML file
        // and use it to display the stored notifications and implement the scrolling
        val recyclerView = view.findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationAdapter(notificationViewModel.notifications.value ?: listOf())
        recyclerView.adapter = notificationAdapter

        // update the the notifications list from ViewModel automatically with the observer
        notificationViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            notificationAdapter.updateData(notifications)
        }

        // set the delete history button on the top of the screen
        historyContainer = view.findViewById(R.id.historyContainer)
        deleteButton = view.findViewById(R.id.deleteNotificationButton)
        deleteButton.setOnClickListener {
            deleteNotificationHistory()
        }

        // check if at least one notification is stored and in that case remove notifications banner
        // otherwise, show the top banner with "no notifications" available
        notificationBanner = view.findViewById(R.id.noNotificationContainer)
        if(notificationAdapter.getItemCount() > 0){
            notificationBanner.visibility= View.GONE
        }else{
            deleteButton.visibility= View.GONE
        }

        return view
    }

    // function to delete the entire notification history
    private fun deleteNotificationHistory() {
        // call the method to delete the notification history and show the top banner for "no notifications" available
        notificationViewModel.deleteNotifications()
        notificationBanner.visibility= View.VISIBLE
        deleteButton.visibility= View.GONE
    }

    /*override fun onDestroyView() {
    super.onDestroyView()
    }*/

}