package com.tanh.datsan.ui.home.main

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tanh.datsan.R
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.utils.LocationUtil
import com.tanh.datsan.viewmodel.HomeViewModel
import com.tanh.datsan.viewmodel.UserViewModel
import java.util.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val gpsSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.fetchFieldNearMe()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permission ->
            val isFineLocationGranted = permission[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val isCoarseLocationGranted = permission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (isFineLocationGranted || isCoarseLocationGranted) {
                LocationUtil.checkRequestLocationSetting(
                    context = context,
                    onEnabled = { viewModel.fetchFieldNearMe() },
                    onDisabled = { intentSenderRequest -> gpsSettingLauncher.launch(intentSenderRequest) },
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    val fieldList by viewModel.fieldList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sportList by viewModel.fieldTypes.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val userRole by userViewModel.userRole.collectAsState()
    val userName by userViewModel.userName.collectAsState()
    val userAvatar by userViewModel.userAvatarUrl.collectAsState()
    val unreadNotiCount by userViewModel.unreadNotification.collectAsState(0)
    val suggestionMessage by viewModel.suggestionMessage.collectAsState()

    var locationName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        floatingActionButton = {
            if (isLoggedIn && (userRole == "admin" || userRole == "staff")) {
                LargeFloatingActionButton(
                    onClick = onNavigateToScanner,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { paddingValues ->
        CustomRefreshLayout(
            onRefresh = { viewModel.fetchFieldNearMe() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- PREMIUM HERO SECTION WITH MESH GRADIENT ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    MeshGradientHero()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isLoggedIn) "Xin chào," else "Chào mừng,",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isLoggedIn) (userName ?: "Người dùng") else "Khách hàng",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isLoggedIn) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotiCount > 0) {
                                                Badge(containerColor = Color(0xFFF87171)) {
                                                    Text(unreadNotiCount.toString(), color = Color.White)
                                                }
                                            }
                                        }
                                    ) {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(44.dp).clickable { onNavigateToNotification() }
                                        ) {
                                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                                        }
                                    }
                                } else {
                                    Surface(
                                        onClick = onLoginClick,
                                        color = Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Text(
                                            "Đăng nhập",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                AsyncImage(
                                    model = userAvatar ?: R.drawable.ic_default_avatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Tìm kiếm sân bóng\nngay trong tích tắc!",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 42.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                // --- FLOATING SEARCH BOX ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-40).dp)
                        .shadow(30.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black, spotColor = Color.Blue),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.padding(start = 8.dp))
                        TextField(
                            value = locationName,
                            onValueChange = { locationName = it },
                            placeholder = { Text("Bạn muốn chơi ở đâu?", color = Color(0xFF94A3B8)) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                viewModel.fetchField(name = locationName.ifBlank { null }, typeId = selectedType)
                                focusManager.clearFocus()
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Tìm", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- SPECIAL OFFERS PAGER ---
                SectionHeader(title = "Ưu đãi đặc biệt", action = "Xem tất cả")
                PromotionPager()

                // --- SPORT CATEGORIES ---
                SectionHeader(title = "Môn thể thao")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        PremiumCategoryChip(
                            label = "Tất cả",
                            isSelected = selectedType == null,
                            onClick = { viewModel.onFieldTypeSelected(null) },
                            icon = Icons.Default.AutoAwesome
                        )
                    }
                    items(sportList) { sport ->
                        PremiumCategoryChip(
                            label = sport.name,
                            isSelected = selectedType == sport.id,
                            onClick = { viewModel.onFieldTypeSelected(sport) },
                            icon = getSportIcon(sport.name)
                        )
                    }
                }

                // --- NEAR YOU SECTION ---
                SectionHeader(title = "Sân bóng gần bạn", subtitle = "Dựa trên vị trí hiện tại")
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(fieldList) { field ->
                        HighEndFieldCard(field = field, onClick = { onNavigateToDetail(field.id) })
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF0F172A), strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang tải dữ liệu...", color = Color(0xFF1E293B), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun MeshGradientHero() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(w * (0.1f + 0.2f * kotlin.math.sin(animProgress * 2 * Math.PI.toFloat())), h * 0.2f),
                    radius = w
                ),
                radius = w,
                center = Offset(w * (0.1f + 0.2f * kotlin.math.sin(animProgress * 2 * Math.PI.toFloat())), h * 0.2f)
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(w * 0.9f, h * (0.1f + 0.3f * kotlin.math.cos(animProgress * 2 * Math.PI.toFloat()))),
                    radius = w * 0.8f
                ),
                radius = w * 0.8f,
                center = Offset(w * 0.9f, h * (0.1f + 0.3f * kotlin.math.cos(animProgress * 2 * Math.PI.toFloat())))
            )
        }
    }
}

@Composable
fun PromotionPager() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        pageSpacing = 16.dp
    ) { page ->
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).absoluteValue
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                    scaleY = lerp(
                        start = 0.9f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = when(page) {
                    0 -> Color(0xFF3B82F6)
                    1 -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
            )
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Giảm giá 30%", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    Text("Đặt sân hôm nay!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                        Text("Mã: DATSAN30", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Icon(Icons.Default.LocalActivity, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, action: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }
        if (action != null) {
            Text(text = action, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun PremiumCategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit, icon: ImageVector) {
    val bg by animateColorAsState(if (isSelected) Color(0xFF1E293B) else Color.White)
    val content by animateColorAsState(if (isSelected) Color.White else Color(0xFF475569))
    
    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = if(!isSelected) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
        modifier = Modifier.shadow(if(isSelected) 8.dp else 0.dp, RoundedCornerShape(16.dp))
    ) {
        Row(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = content)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, color = content, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun HighEndFieldCard(field: FieldModel, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(280.dp).height(360.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                AsyncImage(
                    model = field.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Rating Over Glass
                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = field.rating.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = field.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = field.address ?: "Không có địa chỉ",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Khoảng cách", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("${String.format(Locale.US, "%.1f", field.distance ?: 0.0)} km", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                    Surface(color = Color(0xFFF1F5F9), shape = CircleShape) {
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.padding(8.dp).size(20.dp), tint = Color(0xFF0F172A))
                    }
                }
            }
        }
    }
}

fun getSportIcon(name: String): ImageVector {
    return when {
        name.contains("bóng đá", true) -> Icons.Default.SportsFootball
        name.contains("cầu lông", true) -> Icons.Default.SportsTennis
        name.contains("bóng rổ", true) -> Icons.Default.SportsBasketball
        name.contains("tennis", true) -> Icons.Default.SportsTennis
        name.contains("gym", true) -> Icons.Default.FitnessCenter
        else -> Icons.Default.Sports
    }
}
