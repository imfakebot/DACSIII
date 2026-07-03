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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import coil.compose.AsyncImage
import com.tanh.datsan.R
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.data.model.FieldType
import com.tanh.datsan.ui.component.CustomRefreshLayout
import com.tanh.datsan.utils.LocationUtil
import com.tanh.datsan.utils.toFullImageUrl
import java.util.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    fieldList: List<FieldModel>,
    fieldTypes: List<FieldType>,
    selectedType: String?,
    suggestionMessage: String?,
    isLoading: Boolean,
    userName: String?,
    userAvatarUrl: String?,
    unreadNotification: Int,
    isLoggedIn: Boolean,
    userRole: String,
    onFetchFieldNearMe: () -> Unit,
    onFetchField: (String?, String?, String?, String?) -> Unit,
    onSelectType: (FieldType?) -> Unit,
    onLoginClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
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
            onFetchFieldNearMe()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permission ->
            val isFineLocationGranted =
                permission[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val isCoarseLocationGranted =
                permission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (isFineLocationGranted || isCoarseLocationGranted) {
                LocationUtil.checkRequestLocationSetting(
                    context = context,
                    onEnabled = { onFetchFieldNearMe() },
                    onDisabled = { intentSenderRequest ->
                        gpsSettingLauncher.launch(
                            intentSenderRequest
                        )
                    },
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

    var locationName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            CustomRefreshLayout(
                onRefresh = { onFetchFieldNearMe() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        MeshGradientHero()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .offset(y = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_app_logo),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .requiredSize(60.dp)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isLoggedIn) stringResource(R.string.hello) else stringResource(
                                                R.string.welcome
                                            ),
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (isLoggedIn) (userName
                                                ?: stringResource(id = R.string.main_default_user)) else stringResource(id = R.string.main_guest),
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.wrapContentWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isLoggedIn) {
                                        BadgedBox(
                                            badge = {
                                                if (unreadNotification > 0) {
                                                    Badge(containerColor = Color(0xFFF87171)) {
                                                        Text(
                                                            unreadNotification.toString(),
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            Surface(
                                                color = Color.White.copy(alpha = 0.15f),
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .requiredSize(44.dp)
                                                    .clickable { onNavigateToNotification() }
                                            ) {
                                                Icon(
                                                    Icons.Default.Notifications,
                                                    contentDescription = null, tint = Color.White,
                                                    modifier = Modifier.padding(10.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))
                                    } else {
                                        Surface(
                                            onClick = onLoginClick,
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(44.dp)
                                        ) {
                                            Text(
                                                stringResource(id = R.string.main_login),
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 10.dp
                                                ),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    val fullAvatarUrl = userAvatarUrl.toFullImageUrl()
                                    Box(
                                        modifier = Modifier
                                            .requiredSize(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    ) {
                                        AsyncImage(
                                            model = if (fullAvatarUrl.isNotEmpty()) fullAvatarUrl else R.drawable.ic_default_avatar,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = stringResource(id = R.string.main_hero_title),
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 42.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = (-40).dp)
                            .shadow(
                                30.dp,
                                RoundedCornerShape(24.dp),
                                ambientColor = Color.Black,
                                spotColor = Color.Blue
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            TextField(
                                value = locationName,
                                onValueChange = { locationName = it },
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.main_search_hint),
                                        color = Color(0xFF94A3B8)
                                    )
                                },
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
                                    onFetchField(
                                        null,
                                        null,
                                        selectedType,
                                        locationName.ifBlank { null })
                                    focusManager.clearFocus()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(stringResource(id = R.string.main_search_btn), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    SectionHeader(title = stringResource(id = R.string.main_promo_title), action = stringResource(id = R.string.main_promo_action))
                    PromotionPager()
                    SectionHeader(title = stringResource(id = R.string.main_category_title))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            PremiumCategoryChip(
                                label = stringResource(id = R.string.main_category_all),
                                isSelected = selectedType == null,
                                onClick = { onSelectType(null) },
                                icon = Icons.Default.AutoAwesome
                            )
                        }
                        items(fieldTypes) { sport ->
                            PremiumCategoryChip(
                                label = sport.name,
                                isSelected = selectedType == sport.id,
                                onClick = { onSelectType(sport) },
                                icon = getSportIcon(sport.name)
                            )
                        }
                    }
                    if (!suggestionMessage.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = suggestionMessage,
                                    color = Color(0xFF1E40AF),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    SectionHeader(title = stringResource(id = R.string.main_near_title), subtitle = stringResource(id = R.string.main_near_subtitle))

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
                        Text(
                            stringResource(id = R.string.main_loading),
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                    center = Offset(
                        w * (0.1f + 0.2f * kotlin.math.sin(animProgress * 2 * Math.PI.toFloat())),
                        h * 0.2f
                    ),
                    radius = w
                ),
                radius = w,
                center = Offset(
                    w * (0.1f + 0.2f * kotlin.math.sin(animProgress * 2 * Math.PI.toFloat())),
                    h * 0.2f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(
                        w * 0.9f,
                        h * (0.1f + 0.3f * kotlin.math.cos(animProgress * 2 * Math.PI.toFloat()))
                    ),
                    radius = w * 0.8f
                ),
                radius = w * 0.8f,
                center = Offset(
                    w * 0.9f,
                    h * (0.1f + 0.3f * kotlin.math.cos(animProgress * 2 * Math.PI.toFloat()))
                )
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
                containerColor = when (page) {
                    0 -> Color(0xFF3B82F6)
                    1 -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(id = R.string.main_promo_card_title),
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(id = R.string.main_promo_card_desc),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            stringResource(id = R.string.main_promo_card_code),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    Icons.Default.LocalActivity,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, action: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }
        if (action != null) {
            Text(
                text = action,
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PremiumCategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    val bg by animateColorAsState(if (isSelected) Color(0xFF1E293B) else Color.White)
    val content by animateColorAsState(if (isSelected) Color.White else Color(0xFF475569))

    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
        modifier = Modifier.shadow(if (isSelected) 8.dp else 0.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        modifier = Modifier
            .width(280.dp)
            .height(360.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = field.imageUrl.toFullImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Star,
                            null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = field.rating.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = field.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = field.address ?: stringResource(id = R.string.main_field_no_address),
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(id = R.string.main_field_distance), color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(
                            "${String.format(Locale.US, "%.1f", field.distance ?: 0.0)} km",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(color = Color(0xFFF1F5F9), shape = CircleShape) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp), tint = Color(0xFF0F172A)
                        )
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
