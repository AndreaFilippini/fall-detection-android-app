package com.example.falldetection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.falldetection.ui.dashboard.ListItem

class SharedListViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferencesHelper = SharedPreferencesHelper(application)

    private val _itemList = MutableLiveData(
        sharedPreferencesHelper.loadPriorityList() ?: mutableListOf(
            ListItem("SENDING SMS", 0),
            ListItem("SENDING EMAILS", 1),
            ListItem("CALLING", 2)
        )
    )
    val itemList: LiveData<MutableList<ListItem>> = _itemList

    fun moveItemUp(item: ListItem) {
        val currentList = _itemList.value.orEmpty().toMutableList()
        val currentIndex = currentList.indexOf(item)
        if (currentIndex > 0) {
            // if the item is not already in the first position, move it up
            val newPosition = currentIndex - 1
            // move the item in the list
            currentList.removeAt(currentIndex).also { currentList.add(newPosition, it) }
            // update the LiveData to notify observers
            _itemList.value = currentList
            sharedPreferencesHelper.savePriorityList(currentList)
        }
    }
}