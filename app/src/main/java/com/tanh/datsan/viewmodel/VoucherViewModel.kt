package com.tanh.datsan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.data.repository.VoucherRepository
import com.tanh.datsan.utils.calculateDiscount
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

    private val _selectedVoucher = MutableStateFlow<Voucher?>(null)
    val selectedVoucher: StateFlow<Voucher?> = _selectedVoucher.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

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

    fun selectVoucher(voucher: Voucher?, orderValue: Double) {
        _selectedVoucher.value = voucher
        _discountAmount.value = voucher?.calculateDiscount(orderValue) ?: 0.0
    }

    fun clearSelection() {
        _selectedVoucher.value = null
        _discountAmount.value = 0.0
    }
}
