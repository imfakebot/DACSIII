package com.tanh.datsan.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class GlobalEvent {
    object Logout : GlobalEvent()
}

@Singleton
class GlobalEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<GlobalEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events = _events.asSharedFlow()

    // Dùng trong CoroutineScope (ví dụ: viewModelScope.launch)
    suspend fun emit(event: GlobalEvent) {
        _events.emit(event)
    }
}