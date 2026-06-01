package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.data.repository.FieldRepository
import com.tanh.datsan.utils.LocationHelper
import com.tanh.datsan.utils.toFullImageUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fieldRepository: FieldRepository,
    private val locationHelper: LocationHelper,
) : ViewModel() {

    private val _fieldList = MutableStateFlow<List<FieldModel>>(emptyList())
    val fieldList: StateFlow<List<FieldModel>> = _fieldList

    private val _fieldTypes = MutableStateFlow<List<FieldType>>(emptyList())
    val fieldTypes: StateFlow<List<FieldType>> = _fieldTypes

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType

    private val _suggestionMessage = MutableStateFlow<String?>(null)
    val suggestionMessage: StateFlow<String?> = _suggestionMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentLat: String? = null
    private var currentLng: String? = null

    init {
        fetchFieldTypes()
    }

    fun fetchField(
        lat: String? = currentLat,
        lng: String? = currentLng,
        typeId: String? = _selectedType.value,
        name: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(
                    "HomeViewModel",
                    "Fetching fields with lat=$lat, lng=$lng, typeId=$typeId, name=$name"
                )
                val response =
                    fieldRepository.getAllField(lat = lat, lon = lng, typeId = typeId, name = name)

                if(response.metadata.isSuggestion&& !response.metadata.suggestionMessage.isNullOrBlank()){
                    _suggestionMessage.value = response.metadata.suggestionMessage
                } else{
                    _suggestionMessage.value = null
                }
                val mappedList = response.data.map { jsonItem ->
                    val rawUrl = jsonItem.images?.firstOrNull()?.imageUrl
                    val fixedUrl = rawUrl.toFullImageUrl()
                    Log.d("HomeViewModel", "Link gốc: $rawUrl --- Link ĐÃ SỬA: $fixedUrl")

                    val ward = jsonItem.branch.address?.wardName?:jsonItem.branch.address?.ward?.name ?: ""
                    val city = jsonItem.branch.address?.cityName ?: jsonItem.branch.address?.city?.name ?: ""
                    val street = jsonItem.branch.address?.street ?: ""

                    val fullAddress = listOf(street, ward, city)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifBlank { "Địa chỉ không xác định" }
                    FieldModel(
                        id = jsonItem.id,
                        status = jsonItem.status,
                        name = jsonItem.name,
                        address = fullAddress,
                        rating = jsonItem.averageRating,
                        imageUrl = fixedUrl,
                        distance = jsonItem.distance,
                        fieldType = jsonItem.fieldType
                    )
                }
                _fieldList.value = mappedList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching fields: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchFieldNearMe() {
        locationHelper.getCurrentLocation { lat, lon ->
            currentLat = lat
            currentLng = lon
            if (lat != null && lon != null) {
                Log.d("HomeViewModel", "Current location: lat=$lat, lon=$lon")
                fetchField(lat, lon)
            } else {
                fetchField(lat, lon)
                Log.w("HomeViewModel", "Unable to get current location")
            }
        }
    }

    private fun fetchFieldTypes() {
        viewModelScope.launch {
            try {
                _fieldTypes.value = fieldRepository.getAllFieldTypes()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching field types: ${e.message}")
            }
        }
    }

    fun onFieldTypeSelected(type: FieldType?) {
        _selectedType.value = type?.id
        fetchField(currentLat, currentLng, type?.id)
    }
}