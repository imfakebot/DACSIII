package com.tanh.datsan.ui.state

import com.tanh.datsan.data.model.FieldType

data class AdminFieldTypeState(
    val fieldTypes: List<FieldType> = emptyList(),
    val actionState: ActionState = ActionState.Idle
)
