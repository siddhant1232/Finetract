package com.finetract

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object DebugLogManager {
    private val _logs = MutableLiveData<String>("")
    val logs: LiveData<String> = _logs

    fun log(message: String) {
        val current = _logs.value ?: ""
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _logs.postValue("[$timestamp] $message\n$current")
    }
    
    fun clear() {
        _logs.postValue("")
    }
}
