package com.sportsintercative.contentapp

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

class ContentScreenViewModel(val context: Context) : ViewModel() {

    var isPlaying = MediatorLiveData<Boolean>()


}