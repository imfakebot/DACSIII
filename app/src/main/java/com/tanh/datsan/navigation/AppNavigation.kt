package com.tanh.datsan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tanh.datsan.ui.LoginScreen
import com.tanh.datsan.ui.home.MainScreen
import com.tanh.datsan.ui.RegisterScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home"){
        composable("home"){
            MainScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("login"){
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("register"){
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}