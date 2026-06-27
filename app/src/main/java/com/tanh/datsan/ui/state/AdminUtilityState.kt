package com.tanh.datsan.ui.state

import com.tanh.datsan.data.model.Utility

data class AdminUtilityState(
    val utilities: List<Utility> = emptyList(),
    val actionState: ActionState = ActionState.Idle
)
