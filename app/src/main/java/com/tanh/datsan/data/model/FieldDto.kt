package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName

data class CreateFieldDto(
    val name: String,
    val description: String?,
    val status: Boolean = true,
    @SerializedName("fieldTypeId")
    val fieldTypeId: String,
    @SerializedName("branchId")
    val branchId: String,
    @SerializedName("utilityIds")
    val utilityIds: List<Int>? = null
)

data class UpdateFieldDto(
    val name: String? = null,
    val description: String? = null,
    val status: Boolean? = null,
    @SerializedName("fieldTypeId")
    val fieldTypeId: String? = null,
    @SerializedName("branchId")
    val branchId: String? = null,
    @SerializedName("utilityIds")
    val utilityIds: List<Int>? = null
)

data class CreateFieldTypeDto(
    val name: String,
    val description: String? = null
)

data class UpdateFieldTypeDto(
    val name: String? = null,
    val description: String? = null
)

data class CreateUtilityDto(
    val name: String,
    @SerializedName("iconUrl") val iconUrl: String? = null,
    val price: Double? = null,
    val type: String = "other"
)

data class UpdateUtilityDto(
    val name: String? = null,
    @SerializedName("iconUrl") val iconUrl: String? = null,
    val price: Double? = null,
    val type: String? = null
)
