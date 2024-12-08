package com.example.falldetection
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SharedPhoneViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesHelper = SharedPreferencesHelper(application)

    private val _phoneNumbers = MutableLiveData<List<String>>(emptyList())
    val phoneNumbers: LiveData<List<String>> get() = _phoneNumbers

    init {
        _phoneNumbers.value = sharedPreferencesHelper.loadPhoneNumbers()
    }

    fun addPhoneNumber(phoneNumber: String) {
        val currentList = _phoneNumbers.value?.toMutableList() ?: mutableListOf()
        if (currentList.size < 3) {
            currentList.add(phoneNumber)
            _phoneNumbers.value = currentList.toList()
            sharedPreferencesHelper.savePhoneNumbers(currentList)
        }
    }

    fun removePhoneNumber(phoneNumber: String) {
        val currentList = _phoneNumbers.value?.toMutableList() ?: mutableListOf()
        if (currentList.remove(phoneNumber)) {
            _phoneNumbers.value = currentList.toList()
            sharedPreferencesHelper.savePhoneNumbers(currentList)
        }
    }
}