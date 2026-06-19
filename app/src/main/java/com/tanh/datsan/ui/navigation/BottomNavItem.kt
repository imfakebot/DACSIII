package com.tanh.datsan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Rounded.Home, "Trang chủ")
    object History : BottomNavItem("history", Icons.Rounded.Event, "Lịch đặt")
    object Voucher : BottomNavItem("voucher", Icons.Rounded.CardGiftcard, "Ưu đãi")
    object Profile : BottomNavItem("profile", Icons.Rounded.Person, "Cá nhân")
}

sealed class AdminBottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : AdminBottomNavItem("admin_dashboard", Icons.Rounded.Dashboard, "Tổng quan")
    object Branch : AdminBottomNavItem("admin_branch", Icons.Rounded.Store, "Chi nhánh")
    object Field : AdminBottomNavItem("admin_field", Icons.Rounded.SportsSoccer, "Sân bóng")
    object FieldTypes : AdminBottomNavItem("admin_field_types", Icons.Rounded.Category, "Loại sân")
    object Utilities : AdminBottomNavItem("admin_utilities", Icons.Rounded.Star, "Tiện ích")
    object TimeSlot : AdminBottomNavItem("admin_timeslot", Icons.Rounded.Schedule, "Khung Giờ & Giá")
    object Users : AdminBottomNavItem("admin_users", Icons.Rounded.People, "Người dùng")
    object Bookings : AdminBottomNavItem("admin_bookings", Icons.Rounded.Event, "Đơn đặt sân")
    object QrScanner : AdminBottomNavItem("admin_qr", Icons.Rounded.QrCodeScanner, "Quét mã")
    object Menu : AdminBottomNavItem("admin_menu", Icons.Rounded.Menu, "Menu")
    object Profile : AdminBottomNavItem("admin_profile", Icons.Rounded.Person, "Cá nhân")
}

