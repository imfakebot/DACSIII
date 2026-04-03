package com.tanh.datsan.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        ) {backStackEntry->
            val fieldID = backStackEntry.arguments?.getString("fieldId") ?: return@composable
            Log.d("AppNavigation", "Điều hướng đến DetailScreen với fieldId: $fieldID")
            DetailScreen(
                fieldID,
                onBackClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxSize()
            )
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
    }
}