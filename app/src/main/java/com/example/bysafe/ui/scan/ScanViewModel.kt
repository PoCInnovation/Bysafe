package com.example.bysafe.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "We need an interface to watch the BySafe entities around us"
    }
    val text: LiveData<String> = _text
}
