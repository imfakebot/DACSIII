package com.tanh.datsan.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tanh.datsan.ui.home.AllReviewScreen
import com.tanh.datsan.ui.home.DetailScreen
//import com.tanh.datsan.ui.LoginScreen
import com.tanh.datsan.ui.home.MainScreen

//import com.tanh.datsan.ui.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MainScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                onNavigateToDetail = { fieldId ->
                    navController.navigate("detail/$fieldId")
                }
            )
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
    }
}

//        composable("login"){
//            LoginScreen(
//                onBackClick = {
//                    navController.popBackStack()
//                }
//            )
//        }
//
//        composable("register"){
//            RegisterScreen(
//                onBackClick = {
//                    navController.popBackStack()
//                }
//            )
//        }
