package com.tanh.datsan.utils

import com.tanh.datsan.data.model.VoucherDto

fun VoucherDto.calculateDiscount(orderValue: Double): Double {
    if (orderValue < (this.minOrderValue ?: 0.0)) return 0.0

    return if(this.discountAmount!=null && this.discountAmount>0){
        this.discountAmount
    } else if (this.discountPercentage!=null){
        val pctDiscount = orderValue * (this.discountPercentage/100.0)
        this.maxDiscountAmount?.let { minOf(pctDiscount,it) }?:pctDiscount
    } else{
        0.0
    }
}