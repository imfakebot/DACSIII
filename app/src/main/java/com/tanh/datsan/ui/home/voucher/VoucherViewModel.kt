package com.tanh.datsan.ui.home.voucher

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

import kotlinx.coroutines.flow.update
import com.tanh.datsan.utils.ResponseHelper.parseError
import retrofit2.HttpException

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
                _vouchers.update { response }
            } catch (e: Exception) {
                _vouchers.update { emptyList() }
            }
        }
    }

    fun fetchMyVouchers() {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                val response = voucherRepository.getMyVoucher()
                _myVouchers.update { response }
            } catch (e: Exception) {
                Log.e("VoucherViewModel", "Lỗi tải voucher của tôi: ${e.message}")
                _error.update { "Không thể tải danh sách voucher của bạn" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun fetchCollectibleVouchers() {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                val response = voucherRepository.getCollectibleVouchers()
                Log.d("VoucherViewModel", "Voucher có thể thu thập: ${response.size}")
                _collectibleVouchers.update { response }
            } catch (e: Exception) {
                Log.e("VoucherViewModel", "Lỗi tải voucher có thể thu thập: ${e.message}")
                _error.update { "Không thể tải danh sách voucher mới" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun collectVoucher(voucherId: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                val response = voucherRepository.collectVoucher(voucherId)
                if (response.isSuccessful) {
                    fetchCollectibleVouchers()
                    fetchMyVouchers()
                } else {
                    val message = parseError(response.errorBody()?.string())
                    _error.update { "Thu thập voucher thất bại: $message" }
                }
            } catch (e: Exception) {
                _error.update { "Lỗi kết nối khi thu thập voucher" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun selectVoucher(voucher: Voucher?, orderValue: Double) {
        if (voucher == null) {
            clearSelection()
            return
        }

        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            try {
                val response = voucherRepository.checkVoucher(voucher.code, orderValue)
                _selectedVoucher.update { voucher }
                _discountAmount.update { response.discountAmount }
            } catch (e: Exception) {
                val message = if (e is HttpException) {
                    parseError(e.response()?.errorBody()?.string())
                } else {
                    "Voucher không khả dụng hoặc đã hết hạn"
                }
                _error.update { message }
                _selectedVoucher.update { null }
                _discountAmount.update { 0.0 }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun clearSelection() {
        _selectedVoucher.update { null }
        _discountAmount.update { 0.0 }
        _error.update { null }
    }

    fun clearError() {
        _error.update { null }
    }
}
