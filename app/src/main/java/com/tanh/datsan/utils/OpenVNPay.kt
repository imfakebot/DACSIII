package com.tanh.datsan.utils

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

fun openVNPay(context: Context,url: String){
    val builder = CustomTabsIntent.Builder()

    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, url.toUri())
}