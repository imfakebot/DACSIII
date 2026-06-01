package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.Voucher
import retrofit2.http.GET
import retrofit2.http.Query

interface VoucherApiService {
    @GET(value= "voucher/available")
    suspend fun getAvailableVoucher(@Query("orderValue") orderValue: Double):List<Voucher>

    @GET(value = "voucher/my-vouchers")
    suspend fun getMyVoucher(): List<Voucher>

    @GET(value = "voucher/check")
    suspend fun checkVoucher(
        @Query(value = "code") code: String,
        @Query(value = "orderValue") orderValue: Double
    ): Voucher
}