package com.example.falldetection
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class  SharedEmailViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesHelper = SharedPreferencesHelper(application)

    private val _emailList = MutableLiveData<List<String>>(emptyList())
    val emailList: LiveData<List<String>> get() = _emailList

    init {
        _emailList.value = sharedPreferencesHelper.loadEmails()
    }

    fun addEmail(emailString: String) {
        val currentList = _emailList.value?.toMutableList() ?: mutableListOf()
        if (currentList.size < 3) {
            currentList.add(emailString)
            _emailList.value = currentList.toList()
            sharedPreferencesHelper.saveEmails(currentList)
        }
    }

    fun removeEmail(emailString: String) {
        val currentList = _emailList.value?.toMutableList() ?: mutableListOf()
        if (currentList.remove(emailString)) {
            _emailList.value = currentList.toList()
            sharedPreferencesHelper.saveEmails(currentList)
        }
    }
}