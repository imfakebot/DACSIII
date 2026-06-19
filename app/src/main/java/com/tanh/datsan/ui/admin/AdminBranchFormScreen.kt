package com.tanh.datsan.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanh.datsan.data.model.AvailableManagerDto
import com.tanh.datsan.data.model.BranchDetailDto
import com.tanh.datsan.data.model.CityDto
import com.tanh.datsan.data.model.CreateBranchRequest
import com.tanh.datsan.data.model.WardDto
import com.tanh.datsan.viewmodel.AdminBranchUiState

private val FAccentBlue    = Color(0xFF3D7EF5)
private val FTextPri       = Color(0xFF111827)
private val FTextSec       = Color(0xFF6B7280)
private val FDivider       = Color(0xFFE5E7EB)
private val FCardBg        = Color.White
private val FSectionBg     = Color(0xFFF8F9FC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBranchFormScreen(
    uiState: AdminBranchUiState,
    editingBranch: BranchDetailDto?, // null = Create mode
    onSave: (CreateBranchRequest) -> Unit,
    onWardsCitySelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val isEditMode = editingBranch != null

    // Form state
    var name by remember { mutableStateOf(editingBranch?.name ?: "") }
    var phone by remember { mutableStateOf(editingBranch?.phoneNumber ?: "") }
    var description by remember { mutableStateOf(editingBranch?.description ?: "") }
    var openTime by remember { mutableStateOf(editingBranch?.openTime ?: "06:00:00") }
    var closeTime by remember { mutableStateOf(editingBranch?.closeTime ?: "22:00:00") }
    var street by remember { mutableStateOf(editingBranch?.address?.street ?: "") }
    var latitude by remember { mutableStateOf(editingBranch?.address?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(editingBranch?.address?.longitude?.toString() ?: "") }

    var selectedCity by remember { mutableStateOf<CityDto?>(null) }
    var selectedWard by remember { mutableStateOf<WardDto?>(null) }
    var selectedManager by remember { mutableStateOf<AvailableManagerDto?>(null) }

    var cityDropdownExpanded by remember { mutableStateOf(false) }
    var wardDropdownExpanded by remember { mutableStateOf(false) }
    var managerDropdownExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var streetError by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        nameError = name.isBlank()
        streetError = street.isBlank()
        return !nameError && !streetError && selectedCity != null && selectedWard != null
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FSectionBg)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(elevation = 2.dp),
            color = FCardBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = FTextPri, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isEditMode) "Chỉnh sửa chi nhánh" else "Thêm chi nhánh mới",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FTextPri
                    )
                    Text(if (isEditMode) "Cập nhật thông tin chi nhánh" else "Điền thông tin để tạo chi nhánh", fontSize = 12.sp, color = FTextSec)
                }
            }
        }

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---- Section: Thông tin cơ bản ----
            FormSection(title = "Thông tin cơ bản") {
                FormTextField(
                    value = name, onValueChange = { name = it; nameError = false },
                    label = "Tên chi nhánh *", placeholder = "VD: Sân bóng ABC Quận 1",
                    isError = nameError, errorMsg = "Vui lòng nhập tên chi nhánh",
                    leadingIcon = Icons.Default.Store
                )
                FormTextField(
                    value = phone, onValueChange = { phone = it },
                    label = "Số điện thoại", placeholder = "0987654321",
                    keyboardType = KeyboardType.Phone,
                    leadingIcon = Icons.Default.Phone
                )
                FormTextField(
                    value = description, onValueChange = { description = it },
                    label = "Mô tả", placeholder = "Mô tả về chi nhánh...",
                    leadingIcon = Icons.Default.Info, singleLine = false, maxLines = 3
                )
            }

            // ---- Section: Giờ hoạt động ----
            FormSection(title = "Giờ hoạt động") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormTextField(
                        value = openTime, onValueChange = { openTime = it },
                        label = "Giờ mở cửa", placeholder = "06:00:00",
                        leadingIcon = Icons.Default.WbSunny,
                        modifier = Modifier.weight(1f)
                    )
                    FormTextField(
                        value = closeTime, onValueChange = { closeTime = it },
                        label = "Giờ đóng cửa", placeholder = "22:00:00",
                        leadingIcon = Icons.Default.NightsStay,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ---- Section: Địa chỉ ----
            FormSection(title = "Địa chỉ") {
                FormTextField(
                    value = street, onValueChange = { street = it; streetError = false },
                    label = "Tên đường / Số nhà *", placeholder = "123 Đường Số 4",
                    isError = streetError, errorMsg = "Vui lòng nhập địa chỉ đường",
                    leadingIcon = Icons.Default.LocationOn
                )

                // City Dropdown
                Column {
                    Text("Tỉnh / Thành phố *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = FTextSec, modifier = Modifier.padding(bottom = 6.dp))
                    if (uiState.isLoadingCities) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FAccentBlue)
                    } else {
                        ExposedDropdownMenuBox(expanded = cityDropdownExpanded, onExpandedChange = { cityDropdownExpanded = it }) {
                            OutlinedTextField(
                                value = selectedCity?.name ?: "Chọn Tỉnh/Thành phố",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityDropdownExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                colors = formTextFieldColors(),
                                textStyle = LocalTextStyle.current.copy(color = if (selectedCity == null) FTextSec else FTextPri, fontSize = 14.sp)
                            )
                            ExposedDropdownMenu(expanded = cityDropdownExpanded, onDismissRequest = { cityDropdownExpanded = false }) {
                                uiState.cities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city.name, fontSize = 14.sp) },
                                        onClick = {
                                            selectedCity = city
                                            selectedWard = null
                                            cityDropdownExpanded = false
                                            onWardsCitySelected(city.id.toString())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Ward Dropdown
                Column {
                    Text("Phường / Xã *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = FTextSec, modifier = Modifier.padding(bottom = 6.dp))
                    if (uiState.isLoadingWards) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FAccentBlue)
                    } else {
                        ExposedDropdownMenuBox(expanded = wardDropdownExpanded, onExpandedChange = {
                            if (selectedCity != null) wardDropdownExpanded = it
                        }) {
                            OutlinedTextField(
                                value = selectedWard?.name ?: if (selectedCity == null) "Chọn tỉnh trước" else "Chọn Phường/Xã",
                                onValueChange = {},
                                readOnly = true,
                                enabled = selectedCity != null,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wardDropdownExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                colors = formTextFieldColors(),
                                textStyle = LocalTextStyle.current.copy(color = if (selectedWard == null) FTextSec else FTextPri, fontSize = 14.sp)
                            )
                            ExposedDropdownMenu(expanded = wardDropdownExpanded, onDismissRequest = { wardDropdownExpanded = false }) {
                                uiState.wards.forEach { ward ->
                                    DropdownMenuItem(
                                        text = { Text(ward.name, fontSize = 14.sp) },
                                        onClick = { selectedWard = ward; wardDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // GPS coordinates are hidden from UI to simplify admin UX


            // ---- Section: Quản lý Chi nhánh ----
            FormSection(title = "Gán Quản lý (Tuỳ chọn)") {
                Column {
                    Text("Quản lý chi nhánh (Manager)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = FTextSec, modifier = Modifier.padding(bottom = 6.dp))
                    if (uiState.isLoadingManagers) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FAccentBlue)
                    } else {
                        ExposedDropdownMenuBox(expanded = managerDropdownExpanded, onExpandedChange = { managerDropdownExpanded = it }) {
                            OutlinedTextField(
                                value = selectedManager?.let { it.fullName ?: it.account?.email } ?: "Không gán (để trống)",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FTextSec, modifier = Modifier.size(20.dp)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = managerDropdownExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                colors = formTextFieldColors(),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = if (selectedManager == null) FTextSec else FTextPri)
                            )
                            ExposedDropdownMenu(expanded = managerDropdownExpanded, onDismissRequest = { managerDropdownExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Không gán", fontSize = 14.sp, color = FTextSec) },
                                    onClick = { selectedManager = null; managerDropdownExpanded = false }
                                )
                                uiState.availableManagers.forEach { mgr ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(mgr.fullName ?: mgr.account?.email ?: "Unknown", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                Text(mgr.account?.email ?: "", fontSize = 12.sp, color = FTextSec)
                                            }
                                        },
                                        onClick = { selectedManager = mgr; managerDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (validate()) {
                        val request = CreateBranchRequest(
                            name = name.trim(),
                            phoneNumber = phone.trim(),
                            description = description.trim().ifBlank { null },
                            openTime = openTime.trim(),
                            closeTime = closeTime.trim(),
                            street = street.trim(),
                            cityId = selectedCity!!.id,
                            wardId = selectedWard!!.id,
                            managerId = selectedManager?.id,
                            latitude = 0.0, // Default to 0.0 instead of null to prevent backend crashes
                            longitude = 0.0 // Default to 0.0 instead of null to prevent backend crashes
                        )
                        onSave(request)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = FAccentBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Icon(if (isEditMode) Icons.Default.Save else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditMode) "Lưu thay đổi" else "Tạo chi nhánh", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---- Helpers ----

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FCardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FAccentBlue)
            Divider(color = FDivider, modifier = Modifier.padding(vertical = 10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMsg: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isError) Color(0xFFEF4444) else FTextSec, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = FTextSec.copy(alpha = 0.6f), fontSize = 14.sp) },
            leadingIcon = leadingIcon?.let { icon -> {
                Icon(icon, contentDescription = null, tint = if (isError) Color(0xFFEF4444) else FAccentBlue, modifier = Modifier.size(20.dp))
            }},
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = formTextFieldColors(isError = isError),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = FTextPri)
        )
        if (isError && errorMsg.isNotBlank()) {
            Text(errorMsg, fontSize = 11.sp, color = Color(0xFFEF4444), modifier = Modifier.padding(top = 2.dp, start = 4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun formTextFieldColors(isError: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = if (isError) Color(0xFFEF4444) else FAccentBlue,
    unfocusedBorderColor = if (isError) Color(0xFFEF4444) else FDivider,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFF9FAFB),
    disabledContainerColor = Color(0xFFF3F4F6),
    disabledBorderColor = FDivider,
    disabledTextColor = FTextSec,
    cursorColor = FAccentBlue
)
