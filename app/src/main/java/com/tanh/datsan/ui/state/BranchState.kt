package com.tanh.datsan.ui.state

import com.tanh.datsan.data.model.Branch
import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.WardDto

data class BranchState(
    val branches: List<Branch> = emptyList(),
    val availableManagers: List<com.tanh.datsan.data.model.AccountResponseDto> = emptyList(),
    val selectedBranch: Branch? = null,
    val cities: List<CityDto> = emptyList(),
    val wards: List<WardDto> = emptyList(),
    val isLoadingCities: Boolean = false,
    val isLoadingWards: Boolean = false,
    val actionState: ActionState = ActionState.Idle
)
