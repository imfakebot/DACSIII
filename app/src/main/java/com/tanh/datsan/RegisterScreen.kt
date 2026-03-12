package com.tanh.datsan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(onBackToLogin: () -> Unit) {
    // Khai báo các biến lưu trữ trạng thái người dùng nhập
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var expanded by remember{ mutableStateOf(false)}
    var genderOptions = listOf("nam","nữ","khác")
    var selectedGender by remember {mutableStateOf("")}
    // Định nghĩa màu xanh biển chủ đạo
    val primaryBlue = Color(0xFF1877F2)

    // Biến trạng thái để hỗ trợ cuộn màn hình
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState), // Cho phép cuộn khi form quá dài
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tạo Tài Khoản",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryBlue,
            modifier = Modifier.padding(top = 50.dp)
        )

        Text(
            text = "Điền thông tin để bắt đầu đặt sân bóng",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 30.dp, top = 8.dp)
        )

        // 1. Trường nhập Họ tên
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Trường Email (Bật bàn phím hỗ trợ nhập email)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email liên hệ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Trường Số điện thoại (Bật bàn phím số)
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Số điện thoại") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))


// 4. Trường Giới tính (Dropdown Menu)
        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                // Nếu chưa chọn gì thì hiển thị "--Giới tính--", ngược lại hiện giá trị đã chọn
                value = if (selectedGender.isEmpty()) "--Giới tính--" else selectedGender,
                onValueChange = {},
                readOnly = true, // Không cho phép gõ phím vào đây
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Danh sách xổ xuống
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedGender = selectionOption
                            expanded = false // Chọn xong thì đóng menu lại
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Trường Mật khẩu (Chuyển text thành dấu sao/chấm tròn)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Trường Xác nhận mật khẩu
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Nút Đăng ký
        Button(
            onClick = { /* Xử lý logic đăng ký ở đây */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) {
            Text("Đăng ký", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nút quay lại Đăng nhập
        TextButton(onClick = onBackToLogin) {
            Text(
                text = "Đã có tài khoản? Đăng nhập ngay",
                color = primaryBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Thêm khoảng trống nhỏ ở cuối để cuộn không bị dính sát mép dưới
        Spacer(modifier = Modifier.height(20.dp))
    }
}

