package com.example.docucheckai.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docucheckai.model.AnalysisResult
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun DashboardScreen(
    userName: String,
    recentReports: List<AnalysisResult>,
    onUploadClick: () -> Unit,
    onReportClick: (AnalysisResult) -> Unit,
    onLogout: () -> Unit
) {
    // Advanced Animations State
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Dynamic Greeting Logic
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingMsg = when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    // Statistics Calculation
    val totalChecked = recentReports.size
    val totalIssues = recentReports.sumOf { it.totalIssues }
    val totalPassed = recentReports.sumOf { (5 - it.totalIssues).coerceAtLeast(0) }
    val avgScore = if (totalChecked > 0) recentReports.sumOf { it.score } / totalChecked else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF), Color(0xFFF1F5F9))))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Premium Header Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { -50 })
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF60A5FA)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "$greetingMsg, 👋",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = userName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF172554)
                                )
                            }
                        }
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.background(Color.White, CircleShape).size(42.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // 2. Upload Action Banner (Animated Pulse)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.9f)
                ) {
                    UploadDocumentCard(onUploadClick = onUploadClick)
                }
            }

            // 3. Statistics Overview
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { 50 })) {
                    Text("Overview", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF172554), modifier = Modifier.padding(top = 8.dp))
                }
            }

            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 50 })) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        StatisticCard(modifier = Modifier.weight(1f), icon = Icons.Default.FactCheck, title = "Docs Checked", value = totalChecked.toString(), iconBg = Color(0xFFEFF6FF), iconTint = Color(0xFF2563EB))
                        StatisticCard(modifier = Modifier.weight(1f), icon = Icons.Default.WorkspacePremium, title = "Avg Score", value = "$avgScore%", iconBg = Color(0xFFF0FDF4), iconTint = Color(0xFF16A34A))
                    }
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(900)) + slideInVertically(initialOffsetY = { 50 })) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        StatisticCard(modifier = Modifier.weight(1f), icon = Icons.Default.BugReport, title = "Total Issues", value = totalIssues.toString(), iconBg = Color(0xFFFEF2F2), iconTint = Color(0xFFDC2626))
                        StatisticCard(modifier = Modifier.weight(1f), icon = Icons.Default.TaskAlt, title = "Rules Passed", value = totalPassed.toString(), iconBg = Color(0xFFFFFBEB), iconTint = Color(0xFFD97706))
                    }
                }
            }

            // 4. Recent Documents History
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(1000)) + slideInVertically(initialOffsetY = { 50 })) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent Reports", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF172554), modifier = Modifier.padding(top = 10.dp))
                        if (recentReports.isNotEmpty()) {
                            Text("See All", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }

            if (recentReports.isEmpty()) {
                item {
                    AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(1100))) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No documents analyzed yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF172554))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Upload your first project report to get an AI formatting score.", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            } else {
                items(recentReports) { report ->
                    AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(1100)) + slideInVertically(initialOffsetY = { 50 })) {
                        RecentDocumentItem(report = report, onClick = { onReportClick(report) })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

// =================================================================
// ADVANCED UI COMPONENTS
// =================================================================

@Composable
private fun RecentDocumentItem(report: AnalysisResult, onClick: () -> Unit) {
    val isPdf = report.fileName.endsWith(".pdf", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Smart Icon (Red for PDF, Blue for DOCX)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isPdf) Color(0xFFFEF2F2) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (isPdf) Color(0xFFDC2626) else Color(0xFF2563EB),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.fileName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172554),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (report.score == 100) Icons.Default.Verified else Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (report.score == 100) Color(0xFF16A34A) else Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (report.score == 100) "Score: 100/100 (Perfect)" else "Score: ${report.score}/100 • ${report.totalIssues} Issues",
                        fontSize = 11.sp,
                        color = if (report.score == 100) Color(0xFF16A34A) else Color.Gray,
                        fontWeight = if (report.score == 100) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun UploadDocumentCard(onUploadClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = ""
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.elevatedCardElevation(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF4F46E5))))
                .padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("AI Format Checker", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Scan Your Project\nReport Now", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 32.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUploadClick,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Upload PDF or DOCX", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            }
        }
    }
}

@Composable
private fun StatisticCard(modifier: Modifier, icon: ImageVector, title: String, value: String, iconBg: Color, iconTint: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, modifier = Modifier.size(22.dp), tint = iconTint)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF172554))
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}