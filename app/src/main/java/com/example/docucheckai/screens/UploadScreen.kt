package com.example.docucheckai.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docucheckai.model.AnalysisResult
import com.example.docucheckai.viewmodel.DocumentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun UploadScreen(
    onBack: () -> Unit,
    onReportClick: (AnalysisResult) -> Unit,
    // इथे आपण ViewModel ऍड केला आहे, ज्यामुळे API कॉल करता येईल
    documentViewModel: DocumentViewModel = viewModel()
) {
    val context = LocalContext.current

    // States for File Information
    var selectedFileName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFileUri by rememberSaveable { mutableStateOf<String?>(null) }
    var fileSize by rememberSaveable { mutableStateOf("") }
    var isPdf by rememberSaveable { mutableStateOf(false) }

    // States for AI Analysis
    var isAnalyzing by rememberSaveable { mutableStateOf(false) }
    var progress by rememberSaveable { mutableStateOf(0f) }
    var loadingText by rememberSaveable { mutableStateOf("Uploading to AI Server...") }

    // Smooth Progress Animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    // Strict File Picker (Only PDF & DOCX)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(context, it)
            if (fileName.endsWith(".pdf", ignoreCase = true) || fileName.endsWith(".docx", ignoreCase = true)) {
                selectedFileName = fileName
                selectedFileUri = it.toString()
                isPdf = fileName.endsWith(".pdf", ignoreCase = true)
                fileSize = getFileSize(context, it)

                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(context, "Invalid format! Only PDF or DOCX allowed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Real Backend API Logic
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            progress = 0.4f // 40% progress दाखवा (Uploading)
            loadingText = "Analyzing formatting..."

            if (selectedFileUri != null && !selectedFileName.isNullOrBlank()) {
                val uri = Uri.parse(selectedFileUri!!)

                // ViewModel च्या मदतीने Python Backend ला फाईल पाठवा
                documentViewModel.uploadAndAnalyzeDocument(
                    context = context,
                    uri = uri,
                    fileName = selectedFileName!!,
                    onSuccess = {
                        progress = 1f
                        isAnalyzing = false
                        Toast.makeText(context, "AI Analysis Complete!", Toast.LENGTH_SHORT).show()
                        onReportClick(documentViewModel.currentAnalysis.value!!) // Navigate to Report Screen
                    },
                    onError = { errorMessage ->
                        isAnalyzing = false
                        progress = 0f
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                isAnalyzing = false
                Toast.makeText(context, "File error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // UI Structure (Advanced UI)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            // 1. Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !isAnalyzing,
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .size(42.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF172554))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Upload Document",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF172554)
                    )
                    Text(
                        text = "PDF & DOCX formats supported",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Animated Drop Zone
            val stroke = Stroke(width = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f))
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .scale(if (selectedFileName == null) scale else 1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.7f)) // Glassmorphism effect
                    .drawBehind {
                        val borderColor = if (selectedFileName != null) Color(0xFF16A34A) else Color(0xFF3B82F6)
                        drawRoundRect(
                            color = borderColor.copy(alpha = 0.4f),
                            style = stroke,
                            cornerRadius = CornerRadius(32.dp.toPx())
                        )
                    }
                    .clickable(enabled = !isAnalyzing) {
                        filePickerLauncher.launch(
                            arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    if (selectedFileName == null) {
                        // Empty State
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(46.dp), tint = Color(0xFF2563EB))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Tap to browse files", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF172554))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Strictly .DOCX or .PDF", fontSize = 13.sp, color = Color(0xFF64748B))
                    } else {
                        // File Selected State
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(if (isPdf) Color(0xFFFEF2F2) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(46.dp),
                                tint = if (isPdf) Color(0xFFDC2626) else Color(0xFF2563EB)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = selectedFileName!!,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF172554),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ContainerBadge(text = fileSize, color = Color(0xFF16A34A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. AI Analysis Progress Card
            AnimatedVisibility(
                visible = isAnalyzing,
                enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                exit = fadeOut()
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(loadingText, fontWeight = FontWeight.Bold, color = Color(0xFF172554), fontSize = 15.sp)
                            Text("${(animatedProgress * 100).toInt()}%", fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB), fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF2563EB),
                            trackColor = Color(0xFFEFF6FF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Action Button
            Button(
                onClick = { isAnalyzing = true },
                enabled = selectedFileName != null && !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Color.White, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AI is processing...", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Analyze Formatting", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper Composable for Badges - (Made Private to avoid ambiguity)
@Composable
fun ContainerBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// Helper to get real File Name
private fun getFileName(context: Context, uri: Uri): String {
    var name = ""
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx != -1) name = it.getString(idx)
        }
    }
    return name.ifBlank { uri.lastPathSegment ?: "Document" }
}

// Helper to get formatted File Size
private fun getFileSize(context: Context, uri: Uri): String {
    var size = 0L
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val idx = it.getColumnIndex(OpenableColumns.SIZE)
            if (idx != -1) size = it.getLong(idx)
        }
    }
    return if (size > 0) {
        val kb = size / 1024
        if (kb > 1024) "${kb / 1024} MB" else "$kb KB"
    } else {
        "Unknown Size"
    }
}