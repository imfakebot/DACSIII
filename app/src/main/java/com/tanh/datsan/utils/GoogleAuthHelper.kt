package com.tanh.datsan.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tanh.datsan.BuildConfig
import javax.inject.Inject

class GoogleAuthHelper @Inject constructor() {

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    /**
     * Kích hoạt luồng đăng nhập Google và trả về ID Token bọc trong Result
     */
    suspend fun signInWithGoogle(context: Context, errorUnsupportedMsg: String): Result<String> {
        return try {
            val activity = context.findActivity() 
            if (activity == null) {
                Log.e("GoogleAuthHelper", "Lỗi: Không tìm thấy Activity context")
                return Result.failure(IllegalArgumentException("Activity context is required for Google Sign-In"))
            }

            val credentialManager = CredentialManager.create(context)
            val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

            Log.d("GoogleAuthHelper", "Bắt đầu đăng nhập Google với Client ID: $webClientId")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("GoogleAuthHelper", "Đăng nhập Google thành công, lấy được ID Token")
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Log.e("GoogleAuthHelper", "Loại credential không được hỗ trợ hoặc không phải Google ID Token: ${credential.type}")
                Result.failure(RuntimeException(errorUnsupportedMsg))
            }

        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthHelper", "Đăng nhập thất bại hoặc bị người dùng hủy: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("GoogleAuthHelper", "Lỗi không xác định khi đăng nhập Google: ${e.message}", e)
            Result.failure(e)
        }
    }
}