package com.tanh.datsan.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel // MỚI: Thêm import này
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tanh.datsan.ui.auth.LoginScreen
import com.tanh.datsan.ui.auth.RegisterScreen
import com.tanh.datsan.ui.auth.ResetPasswordScreen
import com.tanh.datsan.ui.auth.VerifyOtpScreen
import com.tanh.datsan.ui.home.MainScreen
import com.tanh.datsan.viewmodel.AuthViewModel // MỚI: Thêm import này

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // MỚI: Khởi tạo dùng chung MỘT ViewModel cho toàn bộ luồng Auth
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // 0. Màn hình Chính
        composable("main") {
            MainScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        // 1. Màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                viewModel = authViewModel, // MỚI: Truyền viewModel vào
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToOtp = { email, isLoginMode ->
                    // Cập nhật lại route vì onNavigateToOtp giờ có 2 tham số
                    navController.navigate("verify_otp/$email/$isLoginMode")
                },
                onNavigateToResetPassword = { email ->
                    navController.navigate("reset_password/$email")
                },
                onNavigateToHome = { userName ->
                    navController.navigate("main") {
                        popUpTo(0)
                    }
                }
            )
        }

        // 2. Màn hình Đăng ký
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel, // MỚI: Truyền viewModel vào
                onBackToLogin = {
                    navController.popBackStack()
                },
                onNavigateToOtp = { email, isLoginMode ->
                    // Cập nhật lại route
                    navController.navigate("verify_otp/$email/$isLoginMode")
                }
            )
        }

        // 3. Màn hình Xác thực OTP
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
                viewModel = authViewModel, // MỚI: Truyền viewModel vào
                email = email,
                isLoginMode = isLoginMode,
                onNavigateToHome = { userName ->
                    navController.navigate("main") {
                        popUpTo(0)
                    }
                },
                onBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }

        // 4. Màn hình Đặt lại mật khẩu
        composable(
            route = "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            ResetPasswordScreen(
                viewModel = authViewModel, // MỚI: Truyền viewModel vào
                emailSent = email,
                onNavigateBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }
    }
}