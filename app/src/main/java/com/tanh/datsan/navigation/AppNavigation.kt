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
import com.tanh.datsan.R
import com.tanh.datsan.data.model.LoginRequest
import com.tanh.datsan.ui.admin.AdminStatisticsScreen
import com.tanh.datsan.ui.admin.booking.AdminCreateBookingScreen
import com.tanh.datsan.ui.admin.branch.BranchFormScreen
import com.tanh.datsan.ui.admin.branch.BranchScreen
import com.tanh.datsan.ui.admin.category.AdminFieldTypeScreen
import com.tanh.datsan.ui.admin.field.AdminFieldScreen
import com.tanh.datsan.ui.admin.field.FieldFormScreen
import com.tanh.datsan.ui.admin.field.FieldImageUploadScreen
import com.tanh.datsan.ui.admin.menu.AdminMenuScreen
import com.tanh.datsan.ui.admin.pricing.AdminTimeSlotScreen

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
import com.tanh.datsan.ui.admin.review.AdminReviewScreen
import com.tanh.datsan.ui.admin.user.AdminUserScreen
import com.tanh.datsan.ui.admin.user.CreateEmployeeScreen
import com.tanh.datsan.ui.home.history.BookingHistoryScreen
import com.tanh.datsan.ui.home.review.MyReviewsScreen
import com.tanh.datsan.ui.home.review.WriteReviewScreen
import com.tanh.datsan.ui.navigation.AdminBottomNavItem
import com.tanh.datsan.ui.navigation.BottomNavItem
import com.tanh.datsan.ui.navigation.MainBottomBar
import com.tanh.datsan.ui.profile.ProfileScreen
import com.tanh.datsan.ui.staff.QrScannerScreen
import androidx.compose.ui.res.stringResource
import com.tanh.datsan.ui.admin.booking.AdminCreateBookingViewModel
import com.tanh.datsan.ui.admin.branch.BranchViewModel
import com.tanh.datsan.ui.admin.field.AdminFieldViewModel
import com.tanh.datsan.ui.admin.pricing.AdminTimeSlotViewModel
import com.tanh.datsan.ui.admin.review.AdminReviewViewModel
import com.tanh.datsan.ui.admin.user.AdminUserViewModel
import com.tanh.datsan.ui.auth.AuthViewModel
import com.tanh.datsan.ui.feedback.FeedbackListViewModel
import com.tanh.datsan.ui.home.booking.BookingSuccessViewModel
import com.tanh.datsan.ui.home.detail.DetailViewModel
import com.tanh.datsan.ui.home.history.BookingHistoryViewModel
import com.tanh.datsan.ui.home.main.HomeViewModel
import com.tanh.datsan.ui.home.notification.NotificationViewModel
import com.tanh.datsan.ui.home.review.ReviewViewModel
import com.tanh.datsan.ui.home.voucher.VoucherViewModel
import com.tanh.datsan.ui.profile.UserViewModel

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
            AdminBottomNavItem.Branch.route,
            AdminBottomNavItem.Field.route,
            AdminBottomNavItem.Users.route,
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
            modifier = Modifier.padding(innerPadding)
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
                val reviewUserViewModel: UserViewModel = hiltViewModel()
                val reviews by reviewViewModel.reviews.collectAsState()
                val reviewMeta by reviewViewModel.reviewMeta.collectAsState()
                val isLoading by reviewViewModel.isLoading.collectAsState()
                val errorMessage by reviewViewModel.errorMessage.collectAsState()
                val isLoggedIn by reviewUserViewModel.isLoggedIn.collectAsState()

                AllReviewScreen(
                    fieldId = fieldId,
                    reviews = reviews,
                    reviewMeta = reviewMeta,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    isLoggedIn = isLoggedIn,
                    onFetchReview = { id -> reviewViewModel.fetchReview(id) },
                    onClearError = { reviewViewModel.clearError() },
                    onBackClick = { navController.popBackStack() },
                    onWriteReviewClick = {
                        navController.navigate("my_reviews")
                    }
                )
            }

            composable(
                "write_review/{bookingId}/{fieldId}/{fieldName}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("fieldId") { type = NavType.StringType },
                    navArgument("fieldName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val fieldName = backStackEntry.arguments?.getString("fieldName")
                    ?: stringResource(R.string.nav_default_field_name)
                val reviewViewModel: ReviewViewModel = hiltViewModel()
                val uiState by reviewViewModel.uiState.collectAsState()

                WriteReviewScreen(
                    bookingId = bookingId,
                    fieldName = fieldName,
                    uiState = uiState,
                    onSubmit = { bId, rating, comment ->
                        reviewViewModel.createReview(bId, rating, comment)
                    },
                    onResetUiState = { reviewViewModel.resetUiState() },
                    onBackClick = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable("my_reviews") {
                val reviewViewModel: ReviewViewModel = hiltViewModel()
                val myReviews by reviewViewModel.myReviews.collectAsState()
                val isLoading by reviewViewModel.isLoading.collectAsState()
                val uiState by reviewViewModel.uiState.collectAsState()

                MyReviewsScreen(
                    myReviews = myReviews,
                    isLoading = isLoading,
                    uiState = uiState,
                    onFetchMyReviews = { reviewViewModel.fetchMyReviews() },
                    onResetUiState = { reviewViewModel.resetUiState() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("admin_reviews") {
                val adminReviewViewModel: AdminReviewViewModel = hiltViewModel()
                val reviews by adminReviewViewModel.reviews.collectAsState()
                val uiState by adminReviewViewModel.uiState.collectAsState()
                val filterRating by adminReviewViewModel.filterRating.collectAsState()

                AdminReviewScreen(
                    reviews = reviews,
                    uiState = uiState,
                    filterRating = filterRating,
                    onFetchReviews = { branchId, rating ->
                        adminReviewViewModel.fetchReviews(branchId, rating)
                    },
                    onDeleteReview = { id -> adminReviewViewModel.deleteReview(id) },
                    onReplyReview = { id, reply -> adminReviewViewModel.replyReview(id, reply) },
                    onResetUiState = { adminReviewViewModel.resetUiState() },
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
                AdminStatisticsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(AdminBottomNavItem.Branch.route) {
                val branchViewModel: BranchViewModel = hiltViewModel()
                val uiState by branchViewModel.uiState.collectAsState()
                val branches = uiState.branches
                val actionState = uiState.actionState

                BranchScreen(
                    branches = branches,
                    actionState = actionState,
                    onFetchBranches = { branchViewModel.fetchBranches() },
                    onNavigateToCreate = { navController.navigate("branch_form") },
                    onNavigateToEdit = { branchId -> navController.navigate("branch_form?branchId=$branchId") },
                    onDeleteBranch = { branchId -> branchViewModel.deleteBranch(branchId) },
                    onResetUiState = { branchViewModel.resetActionState() }
                )
            }

            composable(
                route = "branch_form?branchId={branchId}",
                arguments = listOf(navArgument("branchId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val branchId = backStackEntry.arguments?.getString("branchId")
                val branchViewModel: BranchViewModel = hiltViewModel()
                val uiState by branchViewModel.uiState.collectAsState()
                val actionState = uiState.actionState
                val selectedBranch = uiState.selectedBranch
                val availableManagers = uiState.availableManagers
                val cities = uiState.cities
                val wards = uiState.wards
                val isLoadingCities = uiState.isLoadingCities
                val isLoadingWards = uiState.isLoadingWards

                LaunchedEffect(branchId) {
                    branchViewModel.fetchAvailableManagers()
                    if (branchId != null) {
                        branchViewModel.getBranchById(branchId)
                    } else {
                        branchViewModel.clearSelectedBranch()
                    }
                }

                BranchFormScreen(
                    branchId = branchId,
                    actionState = actionState,
                    selectedBranch = selectedBranch,
                    onFetchBranch = { id -> branchViewModel.getBranchById(id) },
                    onCreateBranch = { dto -> branchViewModel.createBranch(dto) },
                    onUpdateBranch = { id, dto -> branchViewModel.updateBranch(id, dto) },
                    availableManagers = availableManagers,
                    cities = cities,
                    wards = wards,
                    isLoadingCities = isLoadingCities,
                    isLoadingWards = isLoadingWards,
                    onFetchCities = { branchViewModel.fetchCities() },
                    onFetchWards = { cityId -> branchViewModel.fetchWards(cityId) },
                    onBackClick = { navController.popBackStack() },
                    onResetUiState = { branchViewModel.resetActionState() },
                    onClearSelectedBranch = { branchViewModel.clearSelectedBranch() }
                )
            }

            composable(AdminBottomNavItem.Field.route) {
                val adminFieldViewModel: AdminFieldViewModel =
                    hiltViewModel()
                val fields by adminFieldViewModel.fields.collectAsState()
                val uiState by adminFieldViewModel.uiState.collectAsState()
                val branches by adminFieldViewModel.branches.collectAsState()
                val isLoadingMore by adminFieldViewModel.isLoadingMore.collectAsState()

                AdminFieldScreen(
                    userRole = userRole,
                    fields = fields,
                    branches = branches,
                    uiState = uiState,
                    onFetchData = { adminFieldViewModel.fetchInitialData() },
                    onNavigateToCreate = { navController.navigate("field_form") },
                    onNavigateToEdit = { fieldId -> navController.navigate("field_form?fieldId=$fieldId") },
                    onNavigateToUploadImages = { fieldId -> navController.navigate("field_upload_images/$fieldId") },
                    onNavigateToTimeSlot = { fieldId -> navController.navigate("admin_timeslot/$fieldId") },
                    onDeleteField = { fieldId -> adminFieldViewModel.deleteField(fieldId) },
                    onResetUiState = { adminFieldViewModel.resetUiState() },
                    isLoadingMore = isLoadingMore,
                    onLoadMore = { adminFieldViewModel.loadMoreFields() }
                )
            }

            composable(
                route = "field_form?fieldId={fieldId}",
                arguments = listOf(navArgument("fieldId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val fieldId = backStackEntry.arguments?.getString("fieldId")
                val adminFieldViewModel: AdminFieldViewModel =
                    hiltViewModel()
                val uiState by adminFieldViewModel.uiState.collectAsState()
                val selectedField by adminFieldViewModel.selectedField.collectAsState()
                val fieldTypes by adminFieldViewModel.fieldTypes.collectAsState()
                val branches by adminFieldViewModel.branches.collectAsState()
                val utilities by adminFieldViewModel.utilities.collectAsState()

                LaunchedEffect(fieldId) {
                    if (fieldId != null) {
                        adminFieldViewModel.getFieldById(fieldId)
                    } else {
                        adminFieldViewModel.fetchInitialData()
                    }
                }

                FieldFormScreen(
                    fieldId = fieldId,
                    userRole = userRole,
                    uiState = uiState,
                    selectedField = selectedField,
                    fieldTypes = fieldTypes,
                    branches = branches,
                    utilities = utilities,
                    onFetchField = { id -> adminFieldViewModel.getFieldById(id) },
                    onCreateField = { dto -> adminFieldViewModel.createField(dto) },
                    onUpdateField = { id, dto -> adminFieldViewModel.updateField(id, dto) },
                    onBackClick = { navController.popBackStack() },
                    onNavigateToUploadImages = { id ->
                        navController.popBackStack() // close form
                        navController.navigate("field_upload_images/$id")
                    },
                    onNavigateToTimeSlot = {
                        navController.popBackStack() // close form
                        // Wait, fieldId may be null if creating, but we get the new fieldId from uiState!
                        // Actually, wait. When createField returns success, uiState.data contains the new field id.
                        // I need to change FieldFormScreen.kt to pass the new field id to onNavigateToTimeSlot.
                        navController.navigate("admin_timeslot/${it}") // go to timeslots
                    },
                    onResetUiState = { adminFieldViewModel.resetUiState() },
                    onClearSelectedField = { adminFieldViewModel.clearSelectedField() }
                )
            }

            composable(
                route = "field_upload_images/{fieldId}",
                arguments = listOf(navArgument("fieldId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
                val adminFieldViewModel: AdminFieldViewModel =
                    hiltViewModel()
                val uiState by adminFieldViewModel.uiState.collectAsState()

                FieldImageUploadScreen(
                    fieldId = fieldId,
                    uiState = uiState,
                    onUploadImages = { id, uris -> adminFieldViewModel.uploadImages(id, uris) },
                    onBackClick = { navController.popBackStack() },
                    onNavigateNext = {
                        navController.popBackStack() // close upload screen
                        navController.navigate("admin_timeslot/$fieldId")
                    },
                    onResetUiState = { adminFieldViewModel.resetUiState() }
                )
            }

            composable(AdminBottomNavItem.Users.route) {
                val adminUserViewModel: AdminUserViewModel = hiltViewModel()
                val uiState by adminUserViewModel.uiState.collectAsState()
                val users = uiState.users
                val searchQuery = uiState.searchQuery
                val actionState = uiState.actionState

                AdminUserScreen(
                    users = users,
                    actionState = actionState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { adminUserViewModel.updateSearchQuery(it) },
                    onSearch = { adminUserViewModel.fetchUsers() },
                    onFetchUsers = { adminUserViewModel.fetchUsers() },
                    onNavigateToCreateEmployee = { navController.navigate("create_employee") },
                    onToggleActive = { userId, isActive ->
                        adminUserViewModel.toggleActive(
                            userId,
                            isActive
                        )
                    },
                    onResetUiState = { adminUserViewModel.resetActionState() }
                )
            }

            composable("create_employee") {
                val adminUserViewModel: AdminUserViewModel = hiltViewModel()
                val uiState by adminUserViewModel.uiState.collectAsState()
                val branches = uiState.branches
                val actionState = uiState.actionState

                CreateEmployeeScreen(
                    actionState = actionState,
                    branches = branches,
                    onCreateEmployee = { dto -> adminUserViewModel.createEmployee(dto) },
                    onBackClick = { navController.popBackStack() },
                    onResetUiState = { adminUserViewModel.resetActionState() }
                )
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
                val viewModel: BookingHistoryViewModel = hiltViewModel()
                val bookings by viewModel.bookings.collectAsState()
                val uiState by viewModel.uiState.collectAsState()
                val currentStatus by viewModel.currentStatus.collectAsState()

                BookingHistoryScreen(
                    bookings = bookings,
                    uiState = uiState,
                    currentStatus = currentStatus,
                    onFetchBookings = { status -> viewModel.fetchMyBookings(status) },
                    onCancelBooking = { id -> viewModel.cancelBooking(id) },
                    onWriteReview = { bId, fId, fName ->
                        navController.navigate(
                            "write_review/$bId/$fId/${java.net.URLEncoder.encode(fName, "UTF-8")}"
                        )
                    }
                )
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
                val authViewModel: AuthViewModel = hiltViewModel()
                val checkLoggedIn by profileUserViewModel.isLoggedIn.collectAsState()
                if (checkLoggedIn) {
                    ProfileScreen(
                        showBackButton = false,
                        onBackClick = {},
                        onLogoutClick = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToFeedbacks = {
                            navController.navigate("my_feedbacks")
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

            composable(AdminBottomNavItem.Menu.route) {
                AdminMenuScreen(
                    onNavigateToFieldTypes = { navController.navigate("admin_field_types") },
                    onNavigateToUtilities = { navController.navigate("admin_utilities") },
                    onNavigateToBookings = { navController.navigate("admin_bookings") },
                    // onNavigateToTimeSlots is no longer in AdminMenuScreen!
                    // Oh wait, AdminMenuScreen's parameters might need updating too. I'll just remove the parameter there later or keep it unused.
                    onNavigateToTimeSlots = {},
                    onNavigateToReviews = { navController.navigate("admin_reviews") },
                    onNavigateToFeedbacks = { navController.navigate("admin_feedbacks") }
                )
            }

            composable(AdminBottomNavItem.Profile.route) {
                val adminProfileUserViewModel: UserViewModel = hiltViewModel()
                val authViewModel: AuthViewModel = hiltViewModel()
                val adminCheckLoggedIn by adminProfileUserViewModel.isLoggedIn.collectAsState()

                if (adminCheckLoggedIn) {
                    ProfileScreen(
                        showBackButton = false,
                        onBackClick = {},
                        onLogoutClick = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToFeedbacks = {
                            navController.navigate("my_feedbacks")
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            composable(
                "admin_timeslot/{fieldId}",
                arguments = listOf(navArgument("fieldId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
                val viewModel: AdminTimeSlotViewModel = hiltViewModel()
                val timeSlots by viewModel.timeSlots.collectAsState()
                val fields by viewModel.fields.collectAsState()
                val uiState by viewModel.uiState.collectAsState()

                AdminTimeSlotScreen(
                    timeSlots = timeSlots,
                    fields = fields,
                    uiState = uiState,
                    onFetchData = { viewModel.fetchTimeSlots(fieldId) },
                    onUpdateSlot = { id, price, isPeak, fId ->
                        viewModel.updateTimeSlot(id, price, isPeak, fId)
                    },
                    onCreateSlot = { fId, startTime, endTime, price, isPeakHour ->
                        viewModel.createTimeSlot(fId, startTime, endTime, price, isPeakHour)
                    },
                    onResetState = { viewModel.resetUiState() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("admin_field_types") {
                AdminFieldTypeScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("admin_utilities") {
                com.tanh.datsan.ui.admin.category.AdminUtilityScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("admin_bookings") {
                com.tanh.datsan.ui.admin.booking.AdminBookingScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToCreateBooking = {
                        navController.navigate("admin_create_booking")
                    }
                )
            }

            composable("admin_create_booking") {
                val viewModel: AdminCreateBookingViewModel = hiltViewModel()
                val branches by viewModel.branches.collectAsState()
                val fields by viewModel.fields.collectAsState()
                val availableSlots by viewModel.availableSlots.collectAsState()
                val selectedBranch by viewModel.selectedBranch.collectAsState()
                val selectedField by viewModel.selectedField.collectAsState()
                val selectedDate by viewModel.selectedDate.collectAsState()
                val selectedSlot by viewModel.selectedSlot.collectAsState()
                val uiState by viewModel.uiState.collectAsState()
                val selectedDuration by viewModel.selectedDuration.collectAsState()
                val priceState by viewModel.priceState.collectAsState()

                AdminCreateBookingScreen(
                    branches = branches,
                    fields = fields,
                    availableSlots = availableSlots,
                    selectedBranch = selectedBranch,
                    selectedField = selectedField,
                    selectedDate = selectedDate,
                    selectedSlot = selectedSlot,
                    uiState = uiState,
                    onFetchBranches = { viewModel.fetchBranches() },
                    onSelectBranch = { viewModel.selectBranch(it) },
                    onSelectField = { viewModel.selectField(it) },
                    onSelectDate = { viewModel.selectDate(it) },
                    onSelectSlot = { viewModel.selectSlot(it) },
                    onCreateBooking = { name, phone -> viewModel.createBooking(name, phone) },
                    onBackClick = { navController.popBackStack() },
                    onResetUiState = { viewModel.resetUiState() },
                    selectedDuration = selectedDuration,
                    onSelectDuration = { viewModel.selectDuration(it) },
                    priceState = priceState
                )
            }
            composable("admin_voucher") {
                com.tanh.datsan.ui.admin.voucher.AdminVoucherScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("my_feedbacks") {
                val viewModel: FeedbackListViewModel = hiltViewModel()
                com.tanh.datsan.ui.feedback.FeedbackListScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }

            composable("admin_feedbacks") {
                com.tanh.datsan.ui.admin.feedback.AdminFeedbackScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { id -> navController.navigate("admin_feedback_detail/$id") }
                )
            }

            composable(
                route = "admin_feedback_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""

                com.tanh.datsan.ui.admin.feedback.AdminFeedbackDetailScreen(
                    feedbackId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
