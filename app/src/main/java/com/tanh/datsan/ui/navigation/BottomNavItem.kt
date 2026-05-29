package com.tanh.datsan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route:String,val icon: ImageVector,val title:String){
    object Home:BottomNavItem("home", Icons.Rounded.Home,"Trang chủ")
    object History : BottomNavItem("history", Icons.Rounded.Event, "Lịch đặt")
    object Voucher : BottomNavItem("voucher", Icons.Rounded.CardGiftcard, "Ưu đãi")
    object Profile : BottomNavItem("profile", Icons.Rounded.Person, "Cá nhân")
}