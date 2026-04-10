package com.tanh.datsan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tanh.datsan.ui.auth.LoginScreen
import com.tanh.datsan.ui.auth.RegisterScreen
import com.tanh.datsan.ui.auth.ResetPasswordScreen
import com.tanh.datsan.ui.auth.VerifyOtpScreen
import com.tanh.datsan.ui.home.MainScreen // Nhớ import MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main" // MỚI: Đặt màn hình chính làm màn hình khởi chạy
    ) {
        // 0. MỚI: Màn hình Chính (MainScreen)
        composable("main") {
            MainScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        // 1. Màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                // Đăng nhập bước 1 xong -> Truyền email sang trang OTP
                onNavigateToOtp = { email ->
                    navController.navigate("verify_otp/$email/true")
                },
                onNavigateToResetPassword = { email ->
                    navController.navigate("reset_password/$email")
                },
                onNavigateToHome = { userName ->
                    // Đăng nhập thành công -> Về lại MainScreen và xoá stack
                    navController.navigate("main") {
                        popUpTo(0) // Xoá toàn bộ lịch sử trước đó (không cho back lại login)
                    }
                }
            )
        }

        // 2. Màn hình Đăng ký
        composable("register") {
            RegisterScreen(
                onBackToLogin = {
                    navController.popBackStack()
                },
                // Đăng ký bước 1 xong -> Truyền email sang trang OTP
                onNavigateToOtp = { email ->
                    navController.navigate("verify_otp/$email/false")
                }
            )
        }

        // 3. Màn hình Xác thực OTP (Dùng chung cho cả Đăng ký và Đăng nhập)
        composable(
            route = "verify_otp/{email}/{isLoginMode}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("isLoginMode") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val isLoginMode = backStackEntry.arguments?.getBoolean("isLoginMode") ?: true

            VerifyOtpScreen(
                email = email,
                isLoginMode = isLoginMode,
                onNavigateToHome = { userName ->
                    // Xác thực xong -> Xóa sạch lịch sử màn hình trước đó và vào MainScreen
                    navController.navigate("main") {
                        popUpTo(0)
                    }
                },
                onBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }

        // 4. Màn hình Đặt lại mật khẩu (Nhập OTP của Quên mật khẩu)
        composable(
            route = "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            ResetPasswordScreen(
                emailSent = email,
                onNavigateBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }
    }
}