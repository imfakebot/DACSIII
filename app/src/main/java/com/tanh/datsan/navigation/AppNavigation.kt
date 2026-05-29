package com.tanh.datsan.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tanh.datsan.ui.home.review.AllReviewScreen
import com.tanh.datsan.ui.home.detail.DetailScreen
import com.tanh.datsan.ui.home.booking.BookingSuccessScreen
import com.tanh.datsan.ui.staff.QrScannerScreen
//import com.tanh.datsan.ui.LoginScreen
import com.tanh.datsan.ui.home.main.MainScreen
import com.tanh.datsan.ui.home.notification.NotificationScreen
import com.tanh.datsan.ui.navigation.BottomNavItem
import com.tanh.datsan.ui.navigation.MainBottomBar

//import com.tanh.datsan.ui.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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
            composable(BottomNavItem.Home.route) {
                MainScreen(
                    onLoginClick = {
                        navController.navigate("login")
                    },
                    onRegisterClick = {
                        navController.navigate("register")
                    },
                    onNavigateToDetail = { fieldId ->
                        navController.navigate("detail/$fieldId")
                    },
                    onNavigateToScanner = {
                        navController.navigate("scanner")
                    },
                    onNavigateToNotification = {
                        navController.navigate("notification")
                    }
                )
            }

            composable(BottomNavItem.History.route) {
                // TODO: MyBookingsScreen()
            }

            composable(BottomNavItem.Voucher.route) {
                // TODO: VouchersScreen()
            }

            composable(BottomNavItem.Profile.route) {
                // TODO: ProfileScreen()
            }


            composable(
                "detail/{fieldId}",
                listOf(
                    navArgument("fieldId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val fieldID = backStackEntry.arguments?.getString("fieldId") ?: return@composable
                Log.d("AppNavigation", "Điều hướng đến DetailScreen với fieldId: $fieldID")
                DetailScreen(
                    fieldId = fieldID,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToReview = { fieldId ->
                        navController.navigate("reviews/$fieldId")
                    },
                    onNavigateToLogin = {
                        navController.navigate("login")
                    },
                    onNavigateToSuccess = { txnRef ->
                        navController.navigate("booking_success/$txnRef")
                    }
                )
            }

            composable("reviews/{fieldId}") { backStageEntry ->
                val fieldId = backStageEntry.arguments?.getString("fieldId") ?: return@composable
                Log.d("AppNavigation", "Điều hướng đến AllReviewsScreen với fieldId: $fieldId")
                AllReviewScreen(
                    fieldId = fieldId,
                    onBackCLick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                "booking_success/{bookingId}",
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookingId =
                    backStackEntry.arguments?.getString("bookingId") ?: return@composable
                BookingSuccessScreen(
                    bookingId = bookingId,
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("scanner") {
                QrScannerScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("notification") {
                NotificationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

        }
    }
}

