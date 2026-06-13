package com.tanh.datsan.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Import Auth
import com.tanh.datsan.ui.auth.LoginScreen
import com.tanh.datsan.ui.auth.RegisterScreen
import com.tanh.datsan.ui.auth.ResetPasswordScreen
import com.tanh.datsan.ui.auth.VerifyOtpScreen
import com.tanh.datsan.viewmodel.AuthViewModel

// Import Home & Features
import com.tanh.datsan.ui.home.booking.BookingSuccessScreen
import com.tanh.datsan.ui.home.detail.DetailScreen
import com.tanh.datsan.ui.home.main.MainScreen
import com.tanh.datsan.ui.home.notification.NotificationScreen
import com.tanh.datsan.ui.home.review.AllReviewScreen
import com.tanh.datsan.ui.home.voucher.VoucherScreen // Lấy từ Git

// Import Navigation & Profile
import com.tanh.datsan.ui.navigation.BottomNavItem
import com.tanh.datsan.ui.navigation.MainBottomBar
import com.tanh.datsan.ui.profile.ProfileScreen
import com.tanh.datsan.ui.staff.QrScannerScreen
import com.tanh.datsan.ui.admin.AdminAnalyticsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Khởi tạo dùng chung MỘT ViewModel cho toàn bộ luồng Auth từ bản Local
    val authViewModel: AuthViewModel = viewModel()

    // Lấy trạng thái Route hiện tại để ẩn/hiện Bottom Bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        BottomNavItem.Home.route,
        BottomNavItem.History.route,
        BottomNavItem.Voucher.route,
        BottomNavItem.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                MainBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = if (currentRoute in bottomBarRoutes) Modifier.padding(innerPadding) else Modifier
        ) {

            // =======================================================
            // NHÓM 1: CÁC TAB THUỘC BOTTOM NAVIGATION BAR
            // =======================================================

            composable(BottomNavItem.Home.route) {
                MainScreen(
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("register") },
                    onNavigateToDetail = { fieldId -> navController.navigate("detail/$fieldId") },
                    onNavigateToScanner = { navController.navigate("scanner") },
                    onNavigateToNotification = { navController.navigate("notification") }
                )
            }

            composable(BottomNavItem.History.route) {
                // TODO: MyBookingsScreen()
            }

            composable(BottomNavItem.Voucher.route) {
                VoucherScreen()
            }


            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(0) // Xóa sạch stack điều hướng khi đăng xuất
                        }
                    },
                    onNavigateToResetPassword = { email ->
                        navController.navigate("reset_password/$email")
                    }
                )
            }

            // =======================================================
            // NHÓM 2: CHI TIẾT & TIỆN ÍCH MỞ RỘNG
            // =======================================================

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
                    onNavigateToSuccess = { txnRef ->
                        navController.navigate("booking_success/$txnRef")
                    }
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

            composable(
                route = "booking_success/{bookingId}",
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
                BookingSuccessScreen(
                    bookingId = bookingId,
                    onNavigateHome = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable("scanner") {
                QrScannerScreen(onBackClick = { navController.popBackStack() })
            }

            composable("notification") {
                NotificationScreen(onBackClick = { navController.popBackStack() })
            }

            composable("admin_analytics") {
                AdminAnalyticsScreen(onBackClick = { navController.popBackStack() })
            }

            // =======================================================
            // NHÓM 3: LUỒNG XÁC THỰC TÀI KHOẢN
            // =======================================================

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
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(0)
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
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(0)
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
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
}