package com.tanh.datsan.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class GlobalEvent {
    object Logout : GlobalEvent()
}

@Singleton
class GlobalEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<GlobalEvent>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: GlobalEvent) {
        _events.emit(event)
    }
}
