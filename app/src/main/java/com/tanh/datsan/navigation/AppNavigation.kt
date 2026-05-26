package com.tanh.datsan.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tanh.datsan.ui.auth.LoginScreen
import com.tanh.datsan.ui.auth.RegisterScreen
import com.tanh.datsan.ui.auth.ResetPasswordScreen
import com.tanh.datsan.ui.auth.VerifyOtpScreen
//import com.tanh.datsan.ui.home.AllReviewScreen
import com.tanh.datsan.ui.home.detail.DetailScreen
import com.tanh.datsan.ui.home.main.MainScreen
import com.tanh.datsan.ui.home.review.AllReviewScreen
//import com.tanh.datsan.ui.profile.ProfileScreen
//import com.tanh.datsan.ui.home.MainScreen
import com.tanh.datsan.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Khởi tạo dùng chung MỘT ViewModel cho toàn bộ luồng Auth để giữ State
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main" // Dùng "main" làm màn hình bắt đầu hợp nhất
    ) {
        // ==========================================
        // NHÓM 1: LUỒNG MÀN HÌNH CHÍNH & CHI TIẾT
        // ==========================================

        composable("main") {
            MainScreen(
                // Điều hướng từ code cũ
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") },
                // Điều hướng từ code mới
                onNavigateToDetail = { fieldId ->
                    navController.navigate("detail/$fieldId")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
//        composable("profile") {
//            ProfileScreen(
//                onBackClick = { navController.popBackStack() },
//                onLogoutClick = {
//                    navController.navigate("main") {
//                        popUpTo(0) // Xóa toàn bộ stack
//                    }
//                }
//            )
//        }

        composable(
            route = "detail/{fieldId}",
            arguments = listOf(navArgument("fieldId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fieldID = backStackEntry.arguments?.getString("fieldId") ?: return@composable
            Log.d("AppNavigation", "Điều hướng đến DetailScreen với fieldId: $fieldID")
            DetailScreen(
                fieldId = fieldID,
                onBackClick = { navController.popBackStack() },
                onNavigateToReview = { fieldId -> navController.navigate("reviews/$fieldId") },
                onNavigateToLogin = { navController.navigate("login") },
                viewModel = TODO(),
                onNavigateToSuccess = TODO()
            )
        }

        composable(
            route = "reviews/{fieldId}",
            arguments = listOf(navArgument("fieldId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fieldId = backStackEntry.arguments?.getString("fieldId") ?: return@composable
            Log.d("AppNavigation", "Điều hướng đến AllReviewsScreen với fieldId: $fieldId")

            AllReviewScreen(
                fieldId = fieldId,
                onBackCLick = { navController.popBackStack() }
            )
        }

        // ==========================================
        // NHÓM 2: LUỒNG XÁC THỰC (AUTH)
        // ==========================================

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToOtp = { email, isLoginMode ->
                    navController.navigate("verify_otp/$email/$isLoginMode")
                },
                onNavigateToResetPassword = { email ->
                    navController.navigate("reset_password/$email")
                },
                onNavigateToHome = { userName ->
                    navController.navigate("main") {
                        popUpTo(0) // Xóa toàn bộ stack sau khi đăng nhập thành công
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() },
                onNavigateToOtp = { email, isLoginMode ->
                    navController.navigate("verify_otp/$email/$isLoginMode")
                }
            )
        }

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
                viewModel = authViewModel,
                email = email,
                isLoginMode = isLoginMode,
                onNavigateToHome = { userName ->
                    navController.navigate("main") {
                        popUpTo(0)
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login"){
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }

        composable(
            route = "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            ResetPasswordScreen(
                viewModel = authViewModel,
                emailSent = email,
                onNavigateBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }
    }
}