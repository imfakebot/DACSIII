package com.tanh.datsan.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tanh.datsan.data.model.LoginRequest
import com.tanh.datsan.ui.auth.ForgotPasswordScreen
import com.tanh.datsan.ui.auth.LoginScreen
import com.tanh.datsan.ui.auth.OtpScreen
import com.tanh.datsan.ui.auth.RegisterScreen
import com.tanh.datsan.ui.auth.ResetPasswordScreen
import com.tanh.datsan.ui.home.booking.BookingSuccessScreen
import com.tanh.datsan.ui.home.detail.DetailScreen
import com.tanh.datsan.ui.home.main.MainScreen
import com.tanh.datsan.ui.home.notification.NotificationScreen
import com.tanh.datsan.ui.home.review.AllReviewScreen
import com.tanh.datsan.ui.home.voucher.VoucherScreen
import com.tanh.datsan.ui.navigation.AdminBottomNavItem
import com.tanh.datsan.ui.navigation.BottomNavItem
import com.tanh.datsan.ui.navigation.MainBottomBar
import com.tanh.datsan.ui.profile.ProfileScreen
import com.tanh.datsan.ui.staff.QrScannerScreen
import com.tanh.datsan.viewmodel.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val userRole by userViewModel.userRole.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val publicRoutes = listOf(
        BottomNavItem.Home.route,
        "login",
        "register",
        "forgot_password",
        "reset_password/{email}",
        "detail/{fieldId}",
        "all_review/{fieldId}"
    )
    val isOtpRoute = currentRoute?.startsWith("otp/") == true

    val bottomBarRoutes = when (userRole) {
        "super_admin", "branch_manager" -> listOf(
            AdminBottomNavItem.Dashboard.route,
            AdminBottomNavItem.QrScanner.route,
            AdminBottomNavItem.Profile.route
        )

        "staff" -> listOf(
            AdminBottomNavItem.QrScanner.route,
            AdminBottomNavItem.Profile.route
        )

        else -> listOf(
            BottomNavItem.Home.route,
            BottomNavItem.History.route,
            BottomNavItem.Voucher.route,
            BottomNavItem.Profile.route
        )
    }

    LaunchedEffect(isLoggedIn, userRole, currentRoute) {
        if (!isLoggedIn && currentRoute != null && currentRoute !in publicRoutes && !isOtpRoute) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else if (isLoggedIn && currentRoute == BottomNavItem.Home.route) {
            if (userRole == "super_admin" || userRole == "branch_manager") {
                navController.navigate(AdminBottomNavItem.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            } else if (userRole == "staff") {
                navController.navigate(AdminBottomNavItem.QrScanner.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                MainBottomBar(navController = navController, userRole = userRole)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = if (currentRoute in bottomBarRoutes) Modifier.padding(innerPadding) else Modifier
        ) {
            composable(BottomNavItem.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                val homeUserViewModel: UserViewModel = hiltViewModel()

                val fieldList by homeViewModel.fieldList.collectAsState()
                val fieldTypes by homeViewModel.fieldTypes.collectAsState()
                val selectedType by homeViewModel.selectedType.collectAsState()
                val suggestionMessage by homeViewModel.suggestionMessage.collectAsState()
                val isLoading by homeViewModel.isLoading.collectAsState()
                val userName by homeUserViewModel.userName.collectAsState()
                val userAvatarUrl by homeUserViewModel.userAvatarUrl.collectAsState()
                val unreadNotification by homeUserViewModel.unreadNotification.collectAsState(0)
                val homeIsLoggedIn by homeUserViewModel.isLoggedIn.collectAsState()
                val homeUserRole by homeUserViewModel.userRole.collectAsState()

                Log.d("AppNavigation", "userName: $userName, isLoggedIn: $homeIsLoggedIn")

                MainScreen(
                    fieldList = fieldList,
                    fieldTypes = fieldTypes,
                    selectedType = selectedType,
                    suggestionMessage = suggestionMessage,
                    isLoading = isLoading,
                    userName = userName,
                    userAvatarUrl = userAvatarUrl,
                    unreadNotification = unreadNotification,
                    isLoggedIn = homeIsLoggedIn,
                    userRole = homeUserRole,
                    onFetchFieldNearMe = { homeViewModel.fetchFieldNearMe() },
                    onFetchField = { lat, lng, typeId, name ->
                        homeViewModel.fetchField(lat, lng, typeId, name)
                    },
                    onSelectType = { type -> homeViewModel.onFieldTypeSelected(type) },
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("register") },
                    onNavigateToDetail = { fieldId -> navController.navigate("detail/$fieldId") },
                    onNavigateToScanner = { navController.navigate(AdminBottomNavItem.QrScanner.route) },
                    onNavigateToNotification = { navController.navigate("notification") }
                )
            }

            composable("login") {
                val authViewModel: AuthViewModel = hiltViewModel()
                val uiState by authViewModel.uiState.collectAsState()

                LoginScreen(
                    uiState = uiState,
                    onLoginClick = { email, password ->
                        authViewModel.initiateLogin(LoginRequest(email, password))
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onForgotPassword = { navController.navigate("forgot_password") },
                    onOtpSent = { email, isRegister ->
                        navController.navigate("otp/$email/$isRegister")
                    },
                    onAuthenticated = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onResetState = { authViewModel.resetState() },
                    onGoogleLoginClick = { idToken ->
                        authViewModel.loginWithGoogle(idToken)
                    }
                )
            }

            composable("register") {
                val authViewModel: AuthViewModel = hiltViewModel()
                val uiState by authViewModel.uiState.collectAsState()

                RegisterScreen(
                    uiState = uiState,
                    onRegisterClick = { request -> authViewModel.initiateRegistration(request) },
                    onNavigateToLogin = { navController.navigate("login") },
                    onOtpSent = { email, isRegister ->
                        navController.navigate("otp/$email/$isRegister")
                    },
                    onResetState = { authViewModel.resetState() }
                )
            }

            composable("forgot_password") {
                val authViewModel: AuthViewModel = hiltViewModel()
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onNavigateToResetPassword = { email ->
                        navController.navigate("reset_password/$email")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                "reset_password/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                val authViewModel: AuthViewModel = hiltViewModel()
                ResetPasswordScreen(
                    viewModel = authViewModel,
                    emailSent = email,
                    onNavigateBackToLogin = {
                        navController.navigate("login") {
                            popUpTo("forgot_password") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                "otp/{email}/{isRegister}",
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType },
                    navArgument("isRegister") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                val isRegister = backStackEntry.arguments?.getBoolean("isRegister") ?: false
                val authViewModel: AuthViewModel = hiltViewModel()
                val uiState by authViewModel.uiState.collectAsState()

                OtpScreen(
                    email = email,
                    isRegister = isRegister,
                    uiState = uiState,
                    onCompleteRegistration = { code ->
                        authViewModel.completeRegistration(email, code)
                    },
                    onCompleteLogin = { code -> authViewModel.completeLogin(email, code) },
                    onNavigateBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    },
                    onResetState = { authViewModel.resetState() }
                )
            }

            composable(
                "detail/{fieldId}",
                arguments = listOf(navArgument("fieldId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
                val detailViewModel: DetailViewModel = hiltViewModel()
                val detailUserViewModel: UserViewModel = hiltViewModel()
                val voucherViewModel: VoucherViewModel = hiltViewModel()

                val uiState by detailViewModel.uiState.collectAsState()
                val bookingState by detailViewModel.bookingState.collectAsState()
                val priceState by detailViewModel.priceState.collectAsState()
                val bookedSlots by detailViewModel.bookedSlots.collectAsState()
                val vouchers by voucherViewModel.vouchers.collectAsState()
                val selectedVoucher by voucherViewModel.selectedVoucher.collectAsState()
                val discountAmount by voucherViewModel.discountAmount.collectAsState()
                val isVoucherLoading by voucherViewModel.isLoading.collectAsState()
                val detailIsLoggedIn by detailUserViewModel.isLoggedIn.collectAsState()

                DetailScreen(
                    fieldId = fieldId,
                    uiState = uiState,
                    bookingState = bookingState,
                    priceState = priceState,
                    bookedSlots = bookedSlots,
                    vouchers = vouchers,
                    selectedVoucher = selectedVoucher,
                    discountAmount = discountAmount,
                    isVoucherLoading = isVoucherLoading,
                    isLoggedIn = detailIsLoggedIn,
                    onFetchFieldDetail = { id -> detailViewModel.fetchFieldDetail(id) },
                    onFetchBookedSlots = { id, date -> detailViewModel.fetchBookedSlots(id, date) },
                    onCheckPrice = { id, startTime, duration ->
                        detailViewModel.checkPrice(id, startTime, duration)
                    },
                    onFetchAvailableVouchers = { price ->
                        voucherViewModel.fetchAvailableVouchers(price)
                    },
                    onCreateBooking = { dto ->
                        detailViewModel.createBooking(
                            dto.fieldId,
                            dto.startTime,
                            dto.durationMinutes,
                            dto.voucherCode
                        )
                    },
                    onSelectVoucher = { voucher, orderValue ->
                        voucherViewModel.selectVoucher(voucher, orderValue)
                    },
                    onBackClick = { navController.popBackStack() },
                    onNavigateToReview = { id -> navController.navigate("all_review/$id") },
                    onNavigateToLogin = { navController.navigate("login") },
                    onNavigateToSuccess = { bId -> navController.navigate("booking_success/$bId") }
                )
            }

            composable(
                "all_review/{fieldId}",
                arguments = listOf(navArgument("fieldId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
                val reviewViewModel: ReviewViewModel = hiltViewModel()
                val reviews by reviewViewModel.reviews.collectAsState()
                val isLoading by reviewViewModel.isLoading.collectAsState()
                val errorMessage by reviewViewModel.errorMessage.collectAsState()

                AllReviewScreen(
                    fieldId = fieldId,
                    reviews = reviews,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onFetchReview = { id -> reviewViewModel.fetchReview(id) },
                    onClearError = { reviewViewModel.clearError() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                "booking_success/{bookingId}",
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val bookingSuccessViewModel: BookingSuccessViewModel = hiltViewModel()
                val uiState by bookingSuccessViewModel.uiState.collectAsState()

                BookingSuccessScreen(
                    bookingId = bookingId,
                    uiState = uiState,
                    onFetchBookingReceipt = { id -> bookingSuccessViewModel.fetchBookingReceipt(id) },
                    onDownloadTicket = { context, bId, code ->
                        bookingSuccessViewModel.downloadTicket(context, bId, code)
                    },
                    onNavigateHome = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateHistory = {
                        navController.navigate(BottomNavItem.History.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(AdminBottomNavItem.QrScanner.route) {
                QrScannerScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AdminBottomNavItem.Dashboard.route) {
                // TODO
            }

            composable("notification") {
                val notificationViewModel: NotificationViewModel = hiltViewModel()
                val notifications by notificationViewModel.notifications.collectAsState()
                val isLoading by notificationViewModel.isLoading.collectAsState()

                NotificationScreen(
                    notifications = notifications,
                    isLoading = isLoading,
                    onBackClick = { navController.popBackStack() },
                    onMarkAsRead = { id -> notificationViewModel.markAsRead(id) },
                    onMarkAllAsRead = { notificationViewModel.markAllAsRead() },
                    onDeleteNotification = { id -> notificationViewModel.deleteNotification(id) },
                    onClearAllNotifications = { notificationViewModel.clearAllNotifications() },
                    onRefresh = { notificationViewModel.fetchNotification() }
                )
            }

            composable(BottomNavItem.History.route) {
                // TODO: Implement History Screen
            }

            composable(BottomNavItem.Voucher.route) {
                val voucherViewModel: VoucherViewModel = hiltViewModel()
                val myVouchers by voucherViewModel.myVouchers.collectAsState()
                val collectibleVouchers by voucherViewModel.collectibleVouchers.collectAsState()
                val isLoading by voucherViewModel.isLoading.collectAsState()

                VoucherScreen(
                    myVouchers = myVouchers,
                    collectibleVouchers = collectibleVouchers,
                    isLoading = isLoading,
                    onFetchCollectibleVouchers = { voucherViewModel.fetchCollectibleVouchers() },
                    onFetchMyVouchers = { voucherViewModel.fetchMyVouchers() },
                    onCollectVoucher = { id -> voucherViewModel.collectVoucher(id) }
                )
            }

            composable(BottomNavItem.Profile.route) {
                val profileUserViewModel: UserViewModel = hiltViewModel()
                val checkLoggedIn by profileUserViewModel.isLoggedIn.collectAsState()
                if (checkLoggedIn) {
                    ProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToResetPassword = { email ->
                            // Điều hướng đến màn hình đổi mật khẩu từ Profile
                            navController.navigate("reset_password/$email")
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo(BottomNavItem.Home.route) { inclusive = false }
                        }
                    }
                }
            }

            composable(AdminBottomNavItem.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToResetPassword = { email ->
                        // Điều hướng đến màn hình đổi mật khẩu từ Profile cho Admin
                        navController.navigate("reset_password/$email")
                    }
                )
            }
        }
    }
}
