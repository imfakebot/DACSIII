package com.tanh.datsan.utils

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tanh.datsan.R
import javax.inject.Inject

class GoogleAuthHelper @Inject constructor() {

    /**
     * Kích hoạt luồng đăng nhập Google và trả về ID Token bọc trong Result
     */
    suspend fun signInWithGoogle(context: Context, errorUnsupportedMsg: String): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val webClientId = context.getString(R.string.default_web_client_id)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Gọi API, có thể quăng ra Exception nếu user hủy hoặc lỗi mạng
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                // Trả về thành công
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Result.failure(RuntimeException(errorUnsupportedMsg))
            }

        } catch (e: GetCredentialException) {
            // Bắt lỗi liên quan đến Credential (VD: Người dùng tắt bottom sheet đăng nhập)
            Log.e("GoogleAuthHelper", "Đăng nhập thất bại hoặc bị hủy", e)
            Result.failure(e)
        } catch (e: Exception) {
            // Bắt các lỗi không lường trước được để tránh crash app
            Log.e("GoogleAuthHelper", "Lỗi không xác định", e)
            Result.failure(e)
        }
    }
}