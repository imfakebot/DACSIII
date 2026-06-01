package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.data.network.VoucherApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val voucherApiService: VoucherApiService
) {
    suspend fun getAvailableVoucher(orderValue: Double): List<Voucher>{
        return voucherApiService.getAvailableVoucher(orderValue)
    }

    suspend fun getMyVoucher(): List<Voucher>{
        return voucherApiService.getMyVoucher();
    }

    suspend fun checkVoucher(code: String, orderValue: Double): Voucher {
        return voucherApiService.checkVoucher(code, orderValue)
    }
}