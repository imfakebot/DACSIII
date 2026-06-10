package com.tanh.datsan.data.network

import com.tanh.datsan.data.model.Voucher
import com.tanh.datsan.data.model.CheckVoucherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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
    ): CheckVoucherResponse

    @GET("voucher/collectible")
    suspend fun getCollectibleVouchers(): List<Voucher>

    @POST("voucher/{id}/collect")
    suspend fun collectVoucher(@Path("id") voucherId: String): Response<Unit>
}