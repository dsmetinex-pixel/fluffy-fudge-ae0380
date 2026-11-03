package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockInGreenLight
import com.example.ui.theme.StockInGreenText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FirebaseConfigDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Firebase Firestore Backend Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyDark
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StockInGreenLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StockInGreen, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Offline-First Persistence Active", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StockInGreenText)
                                Text("All records are saved immediately in local SQLite with real-time reactive Streams.", fontSize = 11.sp, color = StockInGreenText)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "METINEX FIRESTORE COLLECTIONS SCHEMA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                }

                item {
                    FirestoreCollectionCard(
                        name = "products",
                        fields = "productId, productName, hsmCode, category, unit, ratePerUnit, openingStock, minimumStock, currentStock, createdAt, updatedAt"
                    )
                }

                item {
                    FirestoreCollectionCard(
                        name = "stockIn",
                        fields = "transactionId, productId, productName, quantity, unit, previousStock, hsmCode, rate, totalAmount, date, firmName, createdAt, createdBy"
                    )
                }

                item {
                    FirestoreCollectionCard(
                        name = "stockOut",
                        fields = "transactionId, productId, productName, partyName, quantity, unit, previousStock, hsmCode, rate, totalAmount, remainingStock, date, firmName, createdAt, createdBy"
                    )
                }

                item {
                    FirestoreCollectionCard(
                        name = "activityLogs",
                        fields = "logId, userId, userName, action, description, date, timestamp"
                    )
                }

                item {
                    Text(
                        text = "AUTHENTICATION & CONFIGURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Configured User: Admin (dsmetinex@gmail.com)\nDefault Role: Chief Inventory Officer\nGoogle Services Config: Ready for production sync via google-services.json",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun FirestoreCollectionCard(name: String, fields: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                Text(
                    text = "collection('$name')",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fields,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
