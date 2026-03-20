package com.tanh.datsan.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.tanh.datsan.data.model.FieldModel
import com.tanh.datsan.R
import com.tanh.datsan.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MainScreen(
    viewModel: HomeViewModel = viewModel(),
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Công cụ lấy tọa độ GPS
    val fusedLocalClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Khởi tạo bộ xin quyền
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            // ĐƯỢC CẤP QUYỀN -> Lấy GPS rồi gọi API có tọa độ

            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                fusedLocalClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.fetchField(
                            location.latitude.toString(), location.longitude.toString()
                        )
                    } else {
                        // Bật gps nhưng chưa có sóng
                        viewModel.fetchField() // Lấy dữ liệu mặc định nếu không lấy được GPS
                    }
                }.addOnFailureListener {
                    viewModel.fetchField() // Lấy dữ liệu mặc định nếu có lỗi
                }
            } else {
                viewModel.fetchField() // Lấy dữ liệu mặc định nếu không có quyền
            }
        } else {
            // Sửa lỗi: Thêm trường hợp người dùng TỪ CHỐI cấp quyền
            viewModel.fetchField()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            fusedLocalClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.fetchField(
                        location.latitude.toString(), location.longitude.toString()
                    )
                } else {
                    viewModel.fetchField()
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val fieldList by viewModel.fieldList.collectAsState()
    var isSportMenuExpanded by remember { mutableStateOf(false) }
    var selectedSport by remember { mutableStateOf("Môn thể thao") }
    val sports = listOf("Bóng đá", "Tennis", "Cầu lông", "Bóng bàn")
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F7FA) // Màu nền tổng thể xám nhạt
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. KHỐI HEADER MÀU XANH
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0056B3), // Xanh đậm ở trên
                                Color(0xFF00A2FF)  // Xanh nhạt ở dưới
                            )
                        )
                    )
            ) {


                // Các nút góc trên cùng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Logo app",
                        modifier = Modifier.size(80.dp)
                    )

                    if (isLoggedIn) {
                        AsyncImage(
                            model = "",
                            contentDescription ="Avatar người dùng",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onLoginClick() }) {
                                Text("Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onRegisterClick() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text(
                                    "Đăng ký",
                                    color = Color(0xFF007BFF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Chữ Slogan lớn ở giữa
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .align(Alignment.CenterStart)
                        .offset(y = (-20).dp)
                ) {
                    Text(
                        text = "Sport - Đặt sân", color = Color(0xFFFFD700), // Màu vàng nổi bật
                        fontSize = 28.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "thể thao nhanh chóng",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. KHỐI TÌM KIẾM NỔI (Nằm đè lên viền xanh)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp), // Kéo thẻ này chìm vào khối xanh 40dp
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dropdown chọn môn thể thao
                    Box {
                        OutlinedButton(
                            onClick = { isSportMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedSport, color = Color.Gray)
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = isSportMenuExpanded,
                            onDismissRequest = { isSportMenuExpanded = false }) {
                            sports.forEach { sport ->
                                DropdownMenuItem(text = { Text(sport) }, onClick = {
                                    selectedSport = sport
                                    isSportMenuExpanded = false
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Ô nhập địa điểm, tên sân
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Nhập địa điểm hoặc tên sân...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search, contentDescription = null, tint = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedContainerColor = Color(0xFFF8F9FA)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /* TODO: Xử lý tìm kiếm */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                    ) {
                        Text(
                            "Tìm kiếm",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // 3. KHỐI PROMO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Tặng 1 giờ đá free",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0056B3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Áp dụng cho khách hàng mới", style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                    ) {
                        Text("Đặt ngay", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. DANH SÁCH SÂN BÓNG
            SectionTitle(title = "Sân tập gần bạn")
            FieldListHorizontal(fieldList)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String = "") {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
        )
        if (subtitle.isNotEmpty()) {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun FieldListHorizontal(fieldList: List<FieldModel>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(fieldList.size) { index ->
            val field = fieldList[index]
            Card(
                modifier = Modifier
                    .width(180.dp)
                    .height(220.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    AsyncImage(
                        model = field.imageUrl,
                        contentDescription = "Hình ảnh sân bóng",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.LightGray)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            field.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            field.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                field.rating.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}