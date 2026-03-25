package com.tanh.datsan.data.model

import com.google.gson.annotations.SerializedName


data class FieldResponse(
    val id: String,
    val name: String,
    val description: String?,
    val status: Boolean,

    @SerializedName("fieldType")
    val fieldType: FieldType,
    val images: List<FieldImage>?,
    val branch: Branch?,
    val averageRating: Float? ,
    val reviewCount: Float?
)

data class FieldType(
    val id: String,
    val name: String,
    val description: String?
)
data class FieldModel(
    val name: String,
    val address: String,
    val rating: Float?,
    val imageUrl: String = ""
)

data class FieldImage(
    @SerializedName("image_url")
    val imageUrl: String,
    val isCover: Boolean
)


data class Branch(
    val id: String,
    val name: String,

    @SerializedName("phone_number")
    val address: Address?
)

data class Address(
    val id:String,
    val street: String,
    val latitude:String,
    val longitude: String
)