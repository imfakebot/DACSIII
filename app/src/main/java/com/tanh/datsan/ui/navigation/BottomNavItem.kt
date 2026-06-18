package com.tanh.datsan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Rounded.Home, "Trang chủ")
    object History : BottomNavItem("history", Icons.Rounded.Event, "Lịch đặt")
    object Voucher : BottomNavItem("voucher", Icons.Rounded.CardGiftcard, "Ưu đãi")
    object Profile : BottomNavItem("profile", Icons.Rounded.Person, "Cá nhân")
}

sealed class AdminBottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : AdminBottomNavItem("admin_dashboard", Icons.Rounded.Dashboard, "Tổng quan")
    object QrScanner : AdminBottomNavItem("admin_qr", Icons.Rounded.QrCodeScanner, "Quét mã")
    object Profile : AdminBottomNavItem("admin_profile", Icons.Rounded.Person, "Cá nhân")
}

