package com.tanh.datsan.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Generic bottom sheet dùng chung cho chọn thành phố và chọn phường.
 *
 * @param title Tiêu đề sheet (vd: "Thành phố", "Phường/Xã")
 * @param items Danh sách items dạng Pair<Int, String> (id, name)
 * @param selectedId ID đang được chọn (để hiển thị checkmark)
 * @param isLoading Đang tải dữ liệu
 * @param onSelect Callback khi user chọn 1 item
 * @param onDismiss Callback khi đóng sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    title: String,
    items: List<Pair<Int, String>>,
    selectedId: Int?,
    isLoading: Boolean,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                title,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
            LazyColumn {
                items(items.size) { index ->
                    val (id, name) = items[index]
                    ListItem(
                        headlineContent = {
                            Text(name, fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.clickable {
                            onSelect(id)
                            onDismiss()
                        },
                        trailingContent = {
                            if (selectedId == id) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF3B82F6))
                            }
                        }
                    )
                }
            }
        }
    }
}
