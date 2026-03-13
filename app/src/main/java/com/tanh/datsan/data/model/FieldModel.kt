package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName


data class FieldResponse(
    val id: String,
    val name: String,
    val images: List<FieldImage>?,
    val branch: FieldBranch?
)

data class FieldModel(
    val name: String,
    val address: String,
    val rating: Double,
    val imageUrl: String = ""
)

data class FieldImage(
    @SerializedName("image_url") val imageUrl: String,
    val isCover: Boolean
)


data class FieldBranch(
    val name: String,
    val address: FieldAddress?
)

data class FieldAddress(
    val street: String
)