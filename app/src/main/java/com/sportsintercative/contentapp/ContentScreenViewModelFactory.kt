package com.sportsintercative.contentapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ContentScreenViewModelFactory(private val context: Context):ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContentScreenViewModel(context) as T
    }
}