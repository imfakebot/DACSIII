package com.tanh.datsan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoucherViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository
) : ViewModel() {

    private val _vouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val vouchers: StateFlow<List<Voucher>> = _vouchers.asStateFlow()

    private val _myVouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val myVouchers: StateFlow<List<Voucher>> = _myVouchers.asStateFlow()

    private val _collectibleVouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val collectibleVouchers: StateFlow<List<Voucher>> = _collectibleVouchers.asStateFlow()

    private val _selectedVoucher = MutableStateFlow<Voucher?>(null)
    val selectedVoucher: StateFlow<Voucher?> = _selectedVoucher.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchAvailableVouchers(orderValue: Double) {
        viewModelScope.launch {
            try {
                val response = voucherRepository.getAvailableVoucher(orderValue)
                _vouchers.value = response
            } catch (e: Exception) {
                _vouchers.value = emptyList()
            }
        }
    }

    fun fetchMyVouchers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _myVouchers.value = voucherRepository.getMyVoucher()

            } catch (e: Exception) {
                Log.d("VoucherViewModel", "Lỗi tải voucher của tôi: ${e.message}")
                _error.value = "Không thể tải danh sách voucher của bạn"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCollectibleVouchers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = voucherRepository.getCollectibleVouchers()
                Log.d("VoucherViewModel", "Voucher có thể thu thập: ${response.size}")
                _collectibleVouchers.value = response
            } catch (e: Exception) {
                Log.d("VoucherViewModel", "Lỗi tải voucher có thể thu thập: ${e.message}")
                _error.value = "Không thể tải danh sách voucher mới"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun collectVoucher(voucherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = voucherRepository.collectVoucher(voucherId)
                if (response.isSuccessful) {
                    fetchCollectibleVouchers()
                    fetchMyVouchers()
                } else {
                    _error.value = "Thu thập voucher thất bại"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối khi thu thập voucher"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectVoucher(voucher: Voucher?, orderValue: Double) {
        if (voucher == null) {
            clearSelection()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = voucherRepository.checkVoucher(voucher.code, orderValue)
                _selectedVoucher.value = voucher
                _discountAmount.value = response.discountAmount
            } catch (e: Exception) {
                _error.value = "Voucher không khả dụng hoặc đã hết hạn"
                _selectedVoucher.value = null
                _discountAmount.value = 0.0
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelection() {
        _selectedVoucher.value = null
        _discountAmount.value = 0.0
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
