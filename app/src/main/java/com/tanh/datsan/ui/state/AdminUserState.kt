package com.tanh.datsan.ui.state

import com.tanh.datsan.data.model.AccountPaginatedResponseDto
import com.tanh.datsan.data.model.AccountResponseDto
import com.tanh.datsan.data.model.Branch

data class AdminUserState(
    val users: List<AccountResponseDto> = emptyList(),
    val paginationInfo: AccountPaginatedResponseDto? = null,
    val branches: List<Branch> = emptyList(),
    val searchQuery: String = "",
    val actionState: ActionState = ActionState.Idle
)
