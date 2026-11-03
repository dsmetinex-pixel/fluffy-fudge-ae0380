package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.supabase.SupabaseConfig
import com.example.data.remote.supabase.SupabaseSyncStatus
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockInGreenLight
import com.example.ui.theme.StockInGreenText
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.StockOutRedLight
import com.example.ui.theme.StockOutRedText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.InventoryViewModel

@Composable
fun SupabaseCloudDialog(
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val syncStatus by viewModel.supabaseSyncStatus.collectAsState()
    val syncLogs by viewModel.supabaseSyncLogs.collectAsState()

    var showKey by remember { mutableStateOf(false) }
    var showSqlScript by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.testSupabaseConnection()
    }

    val copyToClipboard: (String, String) -> Unit = { text, label ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF3ECF8E).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "Supabase Cloud Database",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDark
                    )
                    Text(
                        text = "Project: ${SupabaseConfig.DEFAULT_PROJECT_ID}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Connection Status Card
                item {
                    when (val status = syncStatus) {
                        is SupabaseSyncStatus.Syncing -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryBlueLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = PrimaryBlue,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = status.message,
                                        fontSize = 12.sp,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        is SupabaseSyncStatus.Connected -> {
                            if (status.tablesReady) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StockInGreenLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StockInGreen, modifier = Modifier.size(22.dp))
                                        Column {
                                            Text(
                                                text = "Connected & Synchronized",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = StockInGreenText
                                            )
                                            Text(
                                                text = status.message,
                                                fontSize = 11.sp,
                                                color = StockInGreenText
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AlertAmberLight,
                                    modifier = Modifier.fillMaxWidth().border(1.dp, AlertAmber, RoundedCornerShape(8.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(20.dp))
                                            Text(
                                                text = "Connected: Tables Need Creation",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Your Supabase project is active, but the PostgreSQL tables (products, stock_in, stock_out, activity_logs) need to be initialized. Click 'Copy SQL Script' below and execute it in your Supabase SQL Editor.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF78350F),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        is SupabaseSyncStatus.Error -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StockOutRedLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StockOutRed, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("Connection Issue", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StockOutRedText)
                                        Text(status.error, fontSize = 11.sp, color = StockOutRedText)
                                    }
                                }
                            }
                        }

                        is SupabaseSyncStatus.Idle -> {
                            Text("Ready to sync.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                // Project Credentials Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SUPABASE ACCOUNT CONFIGURATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )

                            // Project ID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Project ID", fontSize = 10.sp, color = TextMuted)
                                    Text(SupabaseConfig.DEFAULT_PROJECT_ID, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                                IconButton(
                                    onClick = { copyToClipboard(SupabaseConfig.DEFAULT_PROJECT_ID, "Project ID") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                }
                            }

                            // API Key
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("API Key (Publishable / Anon)", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = if (showKey) SupabaseConfig.DEFAULT_API_KEY else "sb_publishable_••••••••••••••••••••",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { showKey = !showKey },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { copyToClipboard(SupabaseConfig.DEFAULT_API_KEY, "API Key") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                    }
                                }
                            }

                            // REST URL
                            Column {
                                Text("PostgREST Endpoint", fontSize = 10.sp, color = TextMuted)
                                Text("${SupabaseConfig.DEFAULT_BASE_URL}/rest/v1/", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                            }
                        }
                    }
                }

                // Quick Action Buttons: Test Connection & Sync All
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testSupabaseConnection() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Ping", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.syncAllToSupabase() },
                            modifier = Modifier.weight(1.2f).testTag("btn_sync_all_supabase"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync All Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 1-Click SQL Script Tool
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("PostgreSQL DDL Schema", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                                    Text("products, stock_in, stock_out, activity_logs", fontSize = 10.sp, color = TextSecondary)
                                }

                                Button(
                                    onClick = { copyToClipboard(SupabaseConfig.SQL_SETUP_SCRIPT, "Supabase SQL Script") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSqlScript = !showSqlScript },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showSqlScript) "Hide SQL Code" else "View SQL Schema Script",
                                    fontSize = 11.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    if (showSqlScript) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = showSqlScript) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NavyDark,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = SupabaseConfig.SQL_SETUP_SCRIPT,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFE2E8F0),
                                        modifier = Modifier.padding(8.dp),
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Real-time PostgREST sync activity logs
                item {
                    Text(
                        text = "RECENT SYNC ACTIVITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                if (syncLogs.isEmpty()) {
                    item {
                        Text(
                            text = "No sync events yet. When you add products or record transactions, events will appear here.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    items(syncLogs.take(15)) { logLine ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CardBackground,
                            modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderLight, RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = logLine,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Close")
            }
        }
    )
}
