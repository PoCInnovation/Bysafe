package com.example.bysafe.ui.calibrate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalibrateViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "We need interfaces to calibrate bluetooth settings"
    }
    val text: LiveData<String> = _text
}
