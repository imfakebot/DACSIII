package com.tanh.datsan.ui.admin.field

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tanh.datsan.viewmodel.AdminFieldUiState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldImageUploadScreen(
    fieldId: String,
    uiState: AdminFieldUiState,
    onUploadImages: (String, List<MultipartBody.Part>) -> Unit,
    onBackClick: () -> Unit,
    onResetUiState: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImageUris = uris
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminFieldUiState.Success -> {
                uiState.message?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                selectedImageUris = emptyList() // clear after success
                onResetUiState()
            }
            is AdminFieldUiState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                onResetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Hình Ảnh Sân") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { launcher.launch("image/*") }) {
                Icon(Icons.Filled.Image, contentDescription = "Select images")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chọn hình ảnh từ thư viện")
            }

            if (selectedImageUris.isNotEmpty()) {
                Text(text = "Đã chọn ${selectedImageUris.size} hình ảnh", style = MaterialTheme.typography.bodyMedium)
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedImageUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        val parts = selectedImageUris.mapNotNull { uri ->
                            createMultipartFromUri(context, uri, "images")
                        }
                        if (parts.isNotEmpty()) {
                            onUploadImages(fieldId, parts)
                        } else {
                            Toast.makeText(context, "Không thể xử lý hình ảnh", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is AdminFieldUiState.Loading
                ) {
                    if (uiState is AdminFieldUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Tải lên")
                    }
                }
            } else {
                Text("Chưa chọn hình ảnh nào", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun createMultipartFromUri(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        MultipartBody.Part.createFormData(partName, file.name, requestBody)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
