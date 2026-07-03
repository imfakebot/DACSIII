package com.tanh.datsan.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

object JwtUtil {
    fun getRoleFromToken(token: String?): String {
        if (token.isNullOrEmpty()) {
            return "user"
        }
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val jsonObject = JSONObject(payload)
                
                // Kiểm tra xem role là object hay string
                val roleObj = jsonObject.opt("role")
                if (roleObj is JSONObject) {
                    roleObj.optString("name", "user")
                } else {
                    jsonObject.optString("role", "user")
                }
            } else {
                "user"
            }
        } catch (e: Exception) {
            Log.d("JWTUtil", "error ${e.message}")
            "user"
        }
    }
}