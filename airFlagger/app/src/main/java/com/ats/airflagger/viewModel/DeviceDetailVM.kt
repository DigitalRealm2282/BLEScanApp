package com.ats.airflagger.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class DeviceDetailVM : ViewModel() {

    val playSoundState = MutableStateFlow<ConnectionState>(ConnectionState.Connecting)

    sealed class ConnectionState {
        object Success : ConnectionState()
        data class Error(val message: String) : ConnectionState()
        object Connecting : ConnectionState()
        object Playing : ConnectionState()
    }

}