package com.tanh.datsan.ui.staff

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tanh.datsan.viewmodel.CheckInUiState
import com.tanh.datsan.viewmodel.QrScannerViewModel
import java.util.concurrent.Executors

import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    viewModel: QrScannerViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showManualInputDialog by rememberSaveable { mutableStateOf(false) }
    var manualBookingCode by rememberSaveable { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quét mã Check-in") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { showManualInputDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Nhập mã thủ công")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                QrCameraPreview(
                    onQrCodeDetected = { code ->
                        if (uiState is CheckInUiState.Idle && !showManualInputDialog) {
                            viewModel.checkIn(code)
                        }
                    }
                )
                
                // Scanning Overlay
                QrScannerOverlay(modifier = Modifier.fillMaxSize())
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Cần quyền truy cập Camera để quét mã",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Cấp quyền")
                    }
                }
            }

            if (showManualInputDialog) {
                AlertDialog(
                    onDismissRequest = { showManualInputDialog = false },
                    title = { Text("Nhập mã thủ công") },
                    text = {
                        Column {
                            Text("Nếu không thể quét mã QR, vui lòng nhập mã đặt sân tại đây.", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = manualBookingCode,
                                onValueChange = { manualBookingCode = it.uppercase() },
                                label = { Text("Mã đặt sân (Ví dụ: DS123456)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (manualBookingCode.isNotBlank()) {
                                    viewModel.checkIn(manualBookingCode.trim())
                                    showManualInputDialog = false
                                    manualBookingCode = ""
                                }
                            }
                        ) {
                            Text("Check-in")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showManualInputDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }

            // UI Overlays for Loading/Success/Error
            when (val state = uiState) {
                is CheckInUiState.Loading -> {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }

                is CheckInUiState.Success -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetState() },
                        title = { Text("Check-in Thành công") },
                        text = {
                            Column {
                                Text("Khách hàng: ${state.booking.customerName ?: "N/A"}", fontWeight = FontWeight.Bold)
                                Text("Sân: ${state.booking.field?.name ?: "N/A"}")
                                Text("Mã đơn: ${state.booking.code}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Thời gian: ${state.booking.startTime} - ${state.booking.endTime}")
                            }
                        },
                        confirmButton = {
                            Button(onClick = { viewModel.resetState() }) {
                                Text("Tiếp tục")
                            }
                        }
                    )
                }

                is CheckInUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetState() },
                        title = { Text("Lỗi Check-in") },
                        text = { Text(state.message) },
                        confirmButton = {
                            Button(onClick = { viewModel.resetState() }) {
                                Text("Thử lại")
                            }
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun QrScannerOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                val scanAreaSize = 250.dp.toPx()
                val left = (size.width - scanAreaSize) / 2
                val top = (size.height - scanAreaSize) / 2
                
                // Draw semi-transparent background around the scan area
                withTransform({
                    clipRect(left, top, left + scanAreaSize, top + scanAreaSize, ClipOp.Difference)
                }) {
                    drawRect(Color.Black.copy(alpha = 0.6f))
                }
                
                // Draw corners
                val cornerLength = 40.dp.toPx()
                val strokeWidth = 4.dp.toPx()
                val color = Color.White
                
                // Top-left
                drawLine(color, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
                drawLine(color, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)
                
                // Top-right
                drawLine(color, Offset(left + scanAreaSize, top), Offset(left + scanAreaSize - cornerLength, top), strokeWidth)
                drawLine(color, Offset(left + scanAreaSize, top), Offset(left + scanAreaSize, top + cornerLength), strokeWidth)
                
                // Bottom-left
                drawLine(color, Offset(left, top + scanAreaSize), Offset(left + cornerLength, top + scanAreaSize), strokeWidth)
                drawLine(color, Offset(left, top + scanAreaSize), Offset(left, top + scanAreaSize - cornerLength), strokeWidth)
                
                // Bottom-right
                drawLine(color, Offset(left + scanAreaSize, top + scanAreaSize), Offset(left + scanAreaSize - cornerLength, top + scanAreaSize), strokeWidth)
                drawLine(color, Offset(left + scanAreaSize, top + scanAreaSize), Offset(left + scanAreaSize, top + scanAreaSize - cornerLength), strokeWidth)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Đưa mã QR vào khung để quét",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QrCameraPreview(
    onQrCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                )

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it ->
                        it.setAnalyzer(executor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            barcode.rawValue?.let { value ->
                                                onQrCodeDetected(value)
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("QrScanner", "Scan failed", it)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("QrScanner", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
