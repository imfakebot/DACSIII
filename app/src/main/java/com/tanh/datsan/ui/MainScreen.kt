package com.tanh.datsan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MainScreen() {
    var isSportMenuExpanded by remember { mutableStateOf(false) }
    var selectedSport by remember { mutableStateOf("Chọn môn thể thao") }
    val sports = listOf("Bóng đá", "Tennis", "Cầu lông", "Bóng bàn")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đặt sân",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }, actions = {
                    TextButton(
                        onClick = {
                            /* TODO: Chuyển sang màn hình login */
                        }) {
                        // Nút đăng nhập
                        Text("Đăng nhập", fontWeight = FontWeight.Bold)
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8F9FA))
        ) {
            // Phần tìm kiếm và dropdown
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Tìm kiếm sân chơi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = isSportMenuExpanded,
                            onDismissRequest = { isSportMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth((0.8f))
                        ) {
                            sports.forEach { sports ->
                                DropdownMenuItem(text = { Text(sports) }, onClick = {
                                    selectedSport = sports
                                    isSportMenuExpanded = false
                                })
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Ô nhập địa điểm, tên sân
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Nhập tên sân...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        )
                    }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(100.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Trải nghiệm tốt hơn", fontWeight = FontWeight.Bold)
                        Text(
                            "Đăng nhập để lưu sân yêu thích",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {},
                    ) {
                        Text("Đăng kí ngay")
                    }
                }
            }
            SectionTitle(title = "Sân bóng nổi bật")
            FieldListHorizontal()
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String = "") {
    Column(modifier = Modifier.padding(16.dp, vertical = 8.dp)) {
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(color = Color.LightGray)
                    )
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            field.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            field.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Lay so diem danh gia that
                            Text(field.rating.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
