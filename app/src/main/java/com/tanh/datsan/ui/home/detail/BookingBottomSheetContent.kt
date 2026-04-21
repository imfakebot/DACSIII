package com.tanh.datsan.ui.home.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tanh.datsan.R
import com.tanh.datsan.data.model.FieldResponse

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingBottomSheetContent(
    field: FieldResponse,
    onConfirm: (date: String, duration: Int, time: String) -> Unit
) {
    val quickDates = remember { getUpcomingDates("Hôm nay") }
    val durations = listOf(60, 90, 120)
    var selectedDate by remember { mutableStateOf(quickDates[0]) }
    var selectedDuration by remember { mutableIntStateOf(durations[0]) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    val timeSlots = remember(selectedDuration, selectedDate) {
        generateSlots(field.branch.openTime, field.branch.closeTime, selectedDuration)
    }

    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            stringResource(R.string.booking_customize_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text("Chọn ngày", Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickDates) { date ->
                FilterChip(
                    selected = selectedDate == date,
                    onClick = { selectedDate = date; selectedTime = null },
                    label = { Text(date.first) })
            }
        }

        Text("Thời lượng (phút)", Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            durations.forEach { dur ->
                FilterChip(
                    selected = selectedDuration == dur,
                    onClick = { selectedDuration = dur; selectedTime = null },
                    label = { Text("$dur") })
            }
        }

        Text("Chọn giờ bắt đầu", Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            timeSlots.forEach { time ->
                FilterChip(
                    selected = selectedTime == time,
                    onClick = { selectedTime = time },
                    label = { Text(time) })
            }
        }

        Button(
            onClick = {
                selectedTime?.let {
                    onConfirm(
                        selectedDate.second,
                        selectedDuration,
                        it
                    )
                }
            },
            enabled = selectedTime != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("XÁC NHẬN", fontWeight = FontWeight.Bold)
        }
    }
}