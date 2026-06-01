package com.tanh.datsan.data.repository

import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.data.model.CheckVoucherResponse
import com.tanh.datsan.data.network.VoucherApiService
import retrofit2.Response
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
        return voucherApiService.getMyVoucher()
    }

    suspend fun checkVoucher(code: String, orderValue: Double): CheckVoucherResponse {
        return voucherApiService.checkVoucher(code, orderValue)
    }

    suspend fun getCollectibleVouchers(): List<Voucher> {
        return voucherApiService.getCollectibleVouchers()
    }

    suspend fun collectVoucher(voucherId: String): Response<Unit> {
        return voucherApiService.collectVoucher(voucherId)
    }
}