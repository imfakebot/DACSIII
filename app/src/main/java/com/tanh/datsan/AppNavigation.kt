package com.tanh.datsan

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AppNavigation() {
    // NavController là vật dụng để điều khiển việc chuyển màn hình
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login" // Màn hình hiện ra đầu tiên
    ) {
        // 1. Định nghĩa màn hình Đăng nhập
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToHome = { userName ->
                    // Chuyển sang màn Home và nhúng tên người dùng vào đường dẫn
                    navController.navigate("home/$userName")
                }
            )
        }

        // 2. Định nghĩa màn hình Đăng ký
        composable("register") {
            RegisterScreen(
                onBackToLogin = {
                    navController.popBackStack() // Quay lại màn hình trước đó
                }
            )
        }

        // 3. Định nghĩa màn hình Home (Nhận dữ liệu tên người dùng)
        composable(
            route = "home/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            // Trích xuất tên người dùng từ URL, nếu không có thì để mặc định là "Khách"
            val userName = backStackEntry.arguments?.getString("userName") ?: "Khách"

            HomeScreen(
                userName = userName,
                onLogout = {
                    // Xóa toàn bộ ngăn xếp màn hình và quay lại màn hình đăng nhập
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
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