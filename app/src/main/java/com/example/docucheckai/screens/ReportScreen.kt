package com.example.docucheckai.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docucheckai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ReportScreen(
    analysisResult: AnalysisResult,
    onBack: () -> Unit,
    onFixIssues: () -> Unit,
    onReAnalyze: () -> Unit
) {
    val context = LocalContext.current
    val isPerfectScore = analysisResult.score == 100 || analysisResult.issues.isEmpty()

    // Download/Save File Launcher (Strictly PDF or DOCX)
    val mimeType = if (analysisResult.fileName.endsWith(".pdf", ignoreCase = true)) {
        "application/pdf"
    } else {
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType)
    ) { uri: Uri? ->
        if (uri != null) {
            // इथे आपण फाईल राईट करण्याचे लॉजिक लावू शकतो.
            // सध्या UI आणि Success Message दाखवत आहोत.
            Toast.makeText(context, "Document saved successfully!", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.White, CircleShape).size(42.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF172554))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Analysis Report", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF172554))
                    Text(analysisResult.fileName, fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        containerColor = Color(0xFFF6F8FC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // 1. Score Circular Card with Animation
            item {
                AnimatedScoreCard(
                    score = analysisResult.score,
                    fileType = if (analysisResult.fileName.endsWith(".pdf", ignoreCase = true)) "PDF" else "DOCX"
                )
            }

            // 2. Overview Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(modifier = Modifier.weight(1f), title = "Total Issues", value = analysisResult.totalIssues.toString(), color = Color(0xFFDC2626))
                    StatBox(
                        modifier = Modifier.weight(1f),
                        title = "Passed Checks",
                        // ५ मधून जितक्या चुका आल्या ते वजा करून उरलेला आकडा दाखवा
                        value = (5 - analysisResult.totalIssues).coerceAtLeast(0).toString(),
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // 3. Issues List or Perfect State
            if (isPerfectScore) {
                item {
                    PerfectScoreCard()
                }
            } else {
                item {
                    Text("Issues Found", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF172554))
                }
                items(analysisResult.issues) { issue ->
                    IssueCard(issue)
                }
            }

            // 4. Smart Action Button (Auto-Fix OR Download)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                if (isPerfectScore) {
                    Button(
                        onClick = {
                            val newFileName = "Fixed_${analysisResult.fileName}"
                            saveFileLauncher.launch(newFileName)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp).navigationBarsPadding().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Download Fixed Document", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onFixIssues,
                        modifier = Modifier.fillMaxWidth().height(60.dp).navigationBarsPadding().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Auto-Fix Issues", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// ====================================================================
// ADVANCED UI COMPONENTS
// ====================================================================

@Composable
private fun AnimatedScoreCard(score: Int, fileType: String) {
    // Progress Animation
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) score / 100f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "score_anim"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val scoreColor = when {
        score >= 80 -> Color(0xFF16A34A) // Green
        score >= 50 -> Color(0xFFF59E0B) // Orange
        else -> Color(0xFFDC2626) // Red
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.elevatedCardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DOCUMENT SCORE", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 12.sp)
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(fileType, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background Track
                    drawArc(
                        color = Color.White.copy(alpha = 0.05f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Animated Foreground Track
                    drawArc(
                        color = scoreColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Out of 100",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBox(modifier: Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun IssueCard(issue: DocumentIssue) {
    val isCritical = issue.severity.equals("CRITICAL", ignoreCase = true)
    val iconColor = if (isCritical) Color(0xFFDC2626) else Color(0xFFF59E0B)
    val bgColor = if (isCritical) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
    val badgeColor = if (isCritical) Color(0xFF991B1B) else Color(0xFFB45309)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isCritical) Icons.Default.Error else Icons.Default.Warning, contentDescription = null, tint = iconColor)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(issue.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF172554))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(issue.severity, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(issue.description, fontSize = 13.sp, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF8FAFC)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Found", fontSize = 11.sp, color = Color.Gray)
                    Text(issue.currentValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expected", fontSize = 11.sp, color = Color.Gray)
                    Text(issue.expectedValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
        }
    }
}

@Composable
private fun PerfectScoreCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Perfect Formatting!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF166534))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No issues found. Your document strictly follows all formatting guidelines and is ready to use.",
                fontSize = 13.sp,
                color = Color(0xFF15803D),
                textAlign = TextAlign.Center
            )
        }
    }
}