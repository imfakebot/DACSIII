package com.tanh.datsan

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppNavigation() {
    // NavController là vật dụng để điều khiển việc chuyển màn hình
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login" // Màn hình hiện ra đầu tiên
    ) {
        // Định nghĩa màn hình Đăng nhập
        composable("login") {
            LoginScreen(onNavigateToRegister = {
                navController.navigate("register")
            })
        }

        // Định nghĩa màn hình Đăng ký
        composable("register") {
            RegisterScreen(onBackToLogin = {
                navController.popBackStack() // Quay lại màn hình trước đó
            })
        }
    }
}

// --- XEM TOÀN BỘ APP TẠI ĐÂY ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WholeAppPreview() {
    AppNavigation()
}
// --- XEM TRƯỚC MÀN HÌNH ĐĂNG KÝ ---
@Preview(showBackground = true, name = "2. Màn hình Đăng Ký")
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onBackToLogin = {})
}