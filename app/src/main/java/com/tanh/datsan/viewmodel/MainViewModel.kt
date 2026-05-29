package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.core.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    val theme: StateFlow<String> = tokenManager.getTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val language: StateFlow<String> = tokenManager.getLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "vi")

    fun setTheme(theme: String) {
        viewModelScope.launch {
            tokenManager.saveTheme(theme)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            tokenManager.saveLanguage(language)
        }
    }
}
