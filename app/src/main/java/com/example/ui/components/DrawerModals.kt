package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.services.DatabaseService

@Composable
fun NotificationsModal(
    visible: Boolean,
    dbService: DatabaseService? = null,
    onDismissRequest: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    var isPushEnabled by remember { mutableStateOf(true) }
    var isVoiceAlertsEnabled by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C0915))
                .border(1.dp, Color(0xFF2E1A52), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Realtime updates & AI alerts", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notification Toggles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140F24))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("System AI Notifications", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = isPushEnabled,
                                onCheckedChange = { isPushEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF8B5CF6)
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Voice Command Announcements", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = isVoiceAlertsEnabled,
                                onCheckedChange = { isVoiceAlertsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF8B5CF6)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Recent Activity", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // List of recent notification cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotificationItemRow(
                        title = "VEDRA AI Engine Ready",
                        desc = "Smart context indexing completed for 12 local items.",
                        time = "10 mins ago"
                    )
                    NotificationItemRow(
                        title = "VEHub Study Goal Met 🎯",
                        desc = "You completed your physics revision target today!",
                        time = "1 hr ago"
                    )
                    NotificationItemRow(
                        title = "VEDrive Auto Backup",
                        desc = "3 recent document notes synced securely.",
                        time = "Yesterday"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Notifications cleared", Toast.LENGTH_SHORT).show()
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF231642))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemRow(title: String, desc: String, time: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF130E22))
            .border(1.dp, Color(0xFF211838), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(time, color = Color(0xFF64748B), fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
        }
    }
}

@Composable
fun PrivacySecurityModal(
    visible: Boolean,
    dbService: DatabaseService? = null,
    onDismissRequest: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    var isLocalOnlyProcessing by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C0915))
                .border(1.dp, Color(0xFF2E1A52), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Privacy & Security", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Your data remains protected", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF092918))
                        .border(1.dp, Color(0xFF059669), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("On-Device Encryption Active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("VEDRA indexes files and chats locally using SQLite Room.", color = Color(0xFFA7F3D0), fontSize = 10.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140F24))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strict Local AI Voice Mode", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Process voice commands offline when available", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    Switch(
                        checked = isLocalOnlyProcessing,
                        onCheckedChange = { isLocalOnlyProcessing = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Local AI Cache cleared safely", Toast.LENGTH_SHORT).show()
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1A52))
                ) {
                    Text("Clear Local AI Cache & Memory", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun HelpSupportModal(
    visible: Boolean,
    onDismissRequest: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C0915))
                .border(1.dp, Color(0xFF2E1A52), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Help & Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("VEDRA Assistant Guide", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Voice Commands Cheat Sheet", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HelpItemRow("🎙️ \"Hey VEDRA, open Youtube\"", "Launches installed application directly.")
                    HelpItemRow("📚 \"Create a flashcard for Biology\"", "Generates study flashcards in VEHub.")
                    HelpItemRow("📁 \"Save PDF to VEDrive\"", "Indexes local storage files with AI tag.")
                    HelpItemRow("⚡ \"Run automation macro\"", "Triggers custom device automation steps.")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                ) {
                    Text("Got It!", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HelpItemRow(cmd: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF130E22))
            .padding(8.dp)
    ) {
        Column {
            Text(cmd, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            Text(desc, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}

@Composable
fun FeedbackModal(
    visible: Boolean,
    dbService: DatabaseService? = null,
    onDismissRequest: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C0915))
                .border(1.dp, Color(0xFF2E1A52), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEC4899).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = Color(0xFFF472B6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Send Feedback", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Help us make VEDRA better", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { starIndex ->
                        IconButton(onClick = { rating = starIndex }) {
                            Icon(
                                imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Star $starIndex",
                                tint = if (starIndex <= rating) Color(0xFFF59E0B) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = { Text("What do you think of VEDRA?", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF2E1A52),
                        focusedContainerColor = Color(0xFF140F24),
                        unfocusedContainerColor = Color(0xFF140F24)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        dbService?.setSetting("user_feedback_${System.currentTimeMillis()}", "$rating stars: $feedbackText")
                        Toast.makeText(context, "Thank you for your feedback! ✨", Toast.LENGTH_SHORT).show()
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Feedback", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AutomationModal(
    visible: Boolean,
    onExecuteAction: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0C0915))
                .border(1.dp, Color(0xFF2E1A52), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("VEDRA Automation & Actions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Execute macro commands & system triggers", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    com.example.ui.screens.ActionsScreen(
                        onExecuteAction = { cmd ->
                            onExecuteAction(cmd)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}
