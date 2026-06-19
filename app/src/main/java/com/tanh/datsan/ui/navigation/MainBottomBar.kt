package com.tanh.datsan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainBottomBar(navController: NavController, userRole: String) {
    val items = when (userRole) {
        "super_admin", "branch_manager" -> listOf(
            AdminBottomNavItem.Dashboard,
            AdminBottomNavItem.Users,
            AdminBottomNavItem.Branches,
            AdminBottomNavItem.QrScanner,
            AdminBottomNavItem.Profile
        )
        "staff" -> listOf(
            AdminBottomNavItem.QrScanner,
            AdminBottomNavItem.Profile
        )
        else -> listOf(
            BottomNavItem.Home,
            BottomNavItem.History,
            BottomNavItem.Voucher,
            BottomNavItem.Profile
        )
    }

    NavigationBar(containerColor = Color.White) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val route = when (item) {
                is BottomNavItem -> item.route
                is AdminBottomNavItem -> item.route
                else -> ""
            }
            val icon = when (item) {
                is BottomNavItem -> item.icon
                is AdminBottomNavItem -> item.icon
                else -> Icons.Rounded.Home
            }
            val title = when (item) {
                is BottomNavItem -> item.title
                is AdminBottomNavItem -> item.title
                else -> ""
            }

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = currentRoute == route,
                alwaysShowLabel = true,
                onClick = {
                    navController.navigate(route) {
                        navController.graph.startDestinationRoute?.let { startRoute ->
                            popUpTo(startRoute) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF007BFF),
                    selectedTextColor = Color(0xFF007BFF),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                )
            )
        }
    }
}