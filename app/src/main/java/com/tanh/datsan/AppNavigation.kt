package com.tanh.datsan

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
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
                    navController.navigate("home/$userName") {
                        popUpTo("login") { inclusive = true }
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

        // 3. MỚI: Màn hình Xác thực OTP (Dùng chung cho cả Đăng ký và Đăng nhập)
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
                    // Xác thực xong -> Xóa sạch lịch sử màn hình trước đó và vào Home
                    navController.navigate("home/$userName") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }

        // 4. Màn hình Home
        composable(
            route = "home/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Khách"
            HomeScreen(
                userName = userName,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }

        // 5. Màn hình Đặt lại mật khẩu (Nhập OTP của Quên mật khẩu)
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