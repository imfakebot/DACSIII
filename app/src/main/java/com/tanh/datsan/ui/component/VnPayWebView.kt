package com.tanh.datsan.ui.component

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VnPayWebView(
    payemtnUrl: String,
    onPaymentSuccess: (String) -> Unit,
    onPaymentFailure: () -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString()

                        if (url?.contains("payment/vnpay_return") == true) {
                            val uri = url.toUri()

                            val responseCode = uri.getQueryParameter("vnp_ResponseCode")
                            val txnRef = uri.getQueryParameter("vnp_TxnRef")
                            if (responseCode == "00") {
                                onPaymentSuccess(txnRef ?: "UNKNOWN")
                            } else {
                                onPaymentFailure()
                            }
                            return true
                        }
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }
                loadUrl(payemtnUrl)
            }
        }
    )
}