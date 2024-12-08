package com.example.falldetection
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class SharedIpViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesHelper = SharedPreferencesHelper(application)

    val ipData = MutableLiveData<Array<Int>>()

    // Function to set the server IP
    fun setIp(ip: Array<Int>) {
        ipData.value = ip
        sharedPreferencesHelper.setIp(ip)
    }

    // Function to get the current server IP
    fun getIp(): Array<Int> {
        return sharedPreferencesHelper.getIp()
    }

    // Function to get the current server IP in string format
    fun getStringIp(): String {
        var baseIp = ""
        val ip : Array<Int> = sharedPreferencesHelper.getIp()
        ip.forEachIndexed { index, number ->
            baseIp += "$number"
            if(index != 3) {
                baseIp += "."
            }
        }
        return baseIp
    }
}
