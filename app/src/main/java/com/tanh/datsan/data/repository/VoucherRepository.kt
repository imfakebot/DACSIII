package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.VoucherDto
import com.tanh.datsan.data.network.VoucherApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val voucherApiService: VoucherApiService
) {
    suspend fun getAvailableVoucher(orderValue: Double): List<VoucherDto>{
        return voucherApiService.getAvailableVoucher(orderValue)
    }

    suspend fun getMyVoucher(): List<VoucherDto>{
        return voucherApiService.getMyVoucher();
    }

    suspend fun checkVoucher(code: String, orderValue: Double): VoucherDto {
        return voucherApiService.checkVoucher(code, orderValue)
    }
}