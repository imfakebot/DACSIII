package com.tanh.datsan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomBar(navController: NavController, userRole: String) {
    var showMoreMenu by remember { mutableStateOf(false) }

    val items = when (userRole) {
        "super_admin", "branch_manager" -> listOf(
            AdminBottomNavItem.Dashboard,
            AdminBottomNavItem.Menu,
            AdminBottomNavItem.QrScanner,
            AdminBottomNavItem.Profile
        )
        "staff" -> listOf(
            AdminBottomNavItem.Menu,
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

            val isSelected = if (route == AdminBottomNavItem.Menu.route) {
                showMoreMenu
            } else {
                currentRoute == route || (route == AdminBottomNavItem.Menu.route && currentRoute in listOf(
                    AdminBottomNavItem.Branch.route,
                    AdminBottomNavItem.Field.route,
                    AdminBottomNavItem.Users.route,
                    AdminBottomNavItem.Bookings.route,
                    AdminBottomNavItem.FieldTypes.route,
                    AdminBottomNavItem.Utilities.route,
                    AdminBottomNavItem.TimeSlot.route
                ))
            }

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = isSelected,
                alwaysShowLabel = true,
                onClick = {
                    if (route == AdminBottomNavItem.Menu.route) {
                        showMoreMenu = true
                    } else {
                        navController.navigate(route) {
                            navController.graph.startDestinationRoute?.let { startRoute ->
                                popUpTo(startRoute) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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

    if (showMoreMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMoreMenu = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Quản lý chức năng",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val menuItems = mutableListOf<AdminBottomNavItem>(
                    AdminBottomNavItem.Bookings
                )
                
                if (userRole == "super_admin" || userRole == "branch_manager") {
                    menuItems.add(AdminBottomNavItem.Branch)
                    menuItems.add(AdminBottomNavItem.Field)
                    menuItems.add(AdminBottomNavItem.FieldTypes)
                    menuItems.add(AdminBottomNavItem.Utilities)
                    menuItems.add(AdminBottomNavItem.TimeSlot)
                }

                if (userRole == "super_admin") {
                    menuItems.add(AdminBottomNavItem.Users)
                }

                menuItems.forEach { menuItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMoreMenu = false
                                navController.navigate(menuItem.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = menuItem.icon,
                            contentDescription = menuItem.title,
                            tint = Color(0xFF007BFF),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = menuItem.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}