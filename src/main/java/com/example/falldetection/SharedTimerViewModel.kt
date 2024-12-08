package com.example.falldetection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class SharedTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesHelper = SharedPreferencesHelper(application)

    // LiveData to store the seconds input
    val secondsData = MutableLiveData<Int>()
    val delayData = MutableLiveData<Int>()
    val soundData = MutableLiveData<Int>()

    // Function to set the seconds
    fun setSeconds(seconds: Int) {
        secondsData.value = seconds
        sharedPreferencesHelper.setTimer(seconds)
    }

    // Function to get the current seconds value
    fun getSeconds(): Int? {
        return sharedPreferencesHelper.getTimer()
    }

    // Function to set delay
    fun setDelay(seconds: Int) {
        delayData.value = seconds
        sharedPreferencesHelper.setDelay(seconds)
    }

    // Function to get the current delay
    fun getDelay(): Int? {
        return sharedPreferencesHelper.getDelay()
    }

    // Function to set sound timer
    fun setSoundTimer(seconds: Int) {
        soundData.value = seconds
        sharedPreferencesHelper.setSoundTimer(seconds)
    }

    // Function to get the sound timer
    fun getSoundTimer(): Int? {
        return sharedPreferencesHelper.getSoundTimer()
    }
}