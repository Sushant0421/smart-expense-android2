package com.example.docucheckai.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docucheckai.model.AnalysisResult
import com.example.docucheckai.model.DocumentIssue
import kotlinx.coroutines.delay

@Composable
fun FixIssuesScreen(
    analysisResult: AnalysisResult,
    onBack: () -> Unit,
    onReAnalyze: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var isFixing by remember { mutableStateOf(false) }
    var allFixed by remember { mutableStateOf(false) }
    var fixingProgress by remember { mutableStateOf(0f) }

    // जे issues fix व्हायचे राहिले आहेत
    val issuesToFix = analysisResult.issues

    // AI Auto-fix Smooth Animation Logic
    LaunchedEffect(isFixing) {
        if (isFixing) {
            // Simulate AI fixing process with progress
            for (i in 1..100) {
                delay(25)
                fixingProgress = i / 100f
            }
            delay(300)
            isFixing = false
            allFixed = true
            Toast.makeText(context, "All formatting issues resolved!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Premium Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !isFixing,
                    modifier = Modifier.background(Color.White, CircleShape).size(42.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF172554))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Fix Formatting",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF172554)
                    )
                    Text(
                        text = "AI Powered Correction",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // 2. Dynamic Content Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Status Header Card
                item {
                    AnimatedStatusCard(isFixing = isFixing, allFixed = allFixed, issueCount = issuesToFix.size, progress = fixingProgress)
                }

                // Issues List (Hides smoothly when fixing starts)
                item {
                    AnimatedVisibility(
                        visible = !isFixing && !allFixed && issuesToFix.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Text(
                                text = "Issues to Resolve",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF172554),
                                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                            )
                            issuesToFix.forEach { issue ->
                                IssueFixCard(issue = issue)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Success Message (Shows when all fixed)
                item {
                    AnimatedVisibility(
                        visible = allFixed,
                        enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.9f),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(50.dp))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Ready for Review!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF166534))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All formatting rules have been successfully applied. You can now re-analyze or download the final document.",
                                fontSize = 14.sp,
                                color = Color(0xFF15803D),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }

            // 3. Smart Action Button (Bottom Fixed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        if (allFixed) {
                            onReAnalyze()
                        } else {
                            isFixing = true
                        }
                    },
                    enabled = !isFixing,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allFixed) Color(0xFF16A34A) else Color(0xFF2563EB),
                        disabledContainerColor = Color(0xFFCBD5E1)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (isFixing) {
                        Text("Applying Fixes...", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(
                            imageVector = if (allFixed) Icons.Default.TaskAlt else Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (allFixed) "Continue to Final Report" else "Fix All Issues Automatically",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// ADVANCED UI COMPONENTS
// ============================================================

@Composable
private fun AnimatedStatusCard(isFixing: Boolean, allFixed: Boolean, issueCount: Int, progress: Float) {
    val bgColor by animateColorAsState(
        targetValue = when {
            allFixed -> Color(0xFFF0FDF4)
            isFixing -> Color(0xFFEFF6FF)
            else -> Color.White
        }, label = "bg_color"
    )

    val iconTint by animateColorAsState(
        targetValue = when {
            allFixed -> Color(0xFF16A34A)
            isFixing -> Color(0xFF2563EB)
            else -> Color(0xFFDC2626)
        }, label = "icon_tint"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFixing) {
                        CircularProgressIndicator(color = iconTint, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    } else {
                        Icon(
                            imageVector = if (allFixed) Icons.Default.DoneAll else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when {
                            allFixed -> "All Issues Fixed!"
                            isFixing -> "AI is working..."
                            else -> "$issueCount Issues Found"
                        },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (allFixed) Color(0xFF166534) else Color(0xFF172554)
                    )
                    Text(
                        text = when {
                            allFixed -> "Formatting is now perfect."
                            isFixing -> "Modifying document properties."
                            else -> "Tap the button below to fix them."
                        },
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = isFixing) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFFDBEAFE)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(progress * 100).toInt()}% completed", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
            }
        }
    }
}

@Composable
private fun IssueFixCard(issue: DocumentIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(issue.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF172554))
            }
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF8FAFC)).padding(12.dp)
                ) {
                    Text("Current (Wrong)", fontSize = 11.sp, color = Color.Gray)
                    Text(issue.currentValue, fontSize = 14.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }

                Icon(Icons.Default.DoubleArrow, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 12.dp))

                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0FDF4)).padding(12.dp)
                ) {
                    Text("AI Will Change To", fontSize = 11.sp, color = Color(0xFF166534))
                    Text(issue.expectedValue, fontSize = 14.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}