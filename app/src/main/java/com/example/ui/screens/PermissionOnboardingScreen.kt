package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.PermissionGroup
import com.example.services.PermissionService

@Composable
fun PermissionOnboardingScreen(
    onComplete: () -> Unit,
    isDismissable: Boolean = false
) {
    val context = LocalContext.current
    var permissionGroups by remember { mutableStateOf(PermissionService.getPermissionGroups()) }
    var grantedStatusMap by remember {
        mutableStateOf(
            permissionGroups.associate { group ->
                group.id to PermissionService.isGroupGranted(context, group)
            }
        )
    }

    var pendingGroupId by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Refresh statuses for all groups
        grantedStatusMap = permissionGroups.associate { group ->
            group.id to PermissionService.isGroupGranted(context, group)
        }
    }

    LaunchedEffect(Unit) {
        grantedStatusMap = permissionGroups.associate { group ->
            group.id to PermissionService.isGroupGranted(context, group)
        }
    }

    val allGranted = grantedStatusMap.values.all { it }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07060B))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("permission_onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Shield Icon Header
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF14121E))
                    .border(1.dp, Color(0xFF232038), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield Security",
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Neural Sync",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Vedra requires access to these sectors to facilitate high-fidelity offline processing.",
                color = Color(0xFF9CA3AF),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Permissions list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(permissionGroups, key = { it.id }) { group ->
                    val isGranted = grantedStatusMap[group.id] ?: false
                    val icon = getIconForGroup(group.iconKey)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF110E1A))
                            .border(
                                1.dp,
                                if (isGranted) Color(0xFF3B2D5A) else Color(0xFF1F1B2E),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                            .testTag("permission_item_${group.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1D1B2A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = group.title,
                                        tint = if (isGranted) Color(0xFFA78BFA) else Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = group.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = group.subtitle,
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Switch(
                                checked = isGranted,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        pendingGroupId = group.id
                                        permissionLauncher.launch(group.permissions.toTypedArray())
                                    } else {
                                        // Trigger re-launch or update
                                        permissionLauncher.launch(group.permissions.toTypedArray())
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF8B5CF6),
                                    uncheckedThumbColor = Color(0xFF9CA3AF),
                                    uncheckedTrackColor = Color(0xFF262235),
                                    uncheckedBorderColor = Color(0xFF37304A)
                                ),
                                modifier = Modifier.testTag("switch_${group.id}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Encryption & Security Notice Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0C0A14))
                    .border(1.dp, Color(0xFF1B1829), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Security Check",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Text(
                        text = "Vedra operates in The Void. Your data never leaves this device. Processing is 100% local and encrypted.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Initialize System Action Button
            Button(
                onClick = {
                    PermissionService.setPermissionsOnboarded(context, true)
                    onComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_initialize_system"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allGranted) Color(0xFF6D28D9) else Color(0xFF231E33),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (allGranted) "INITIALIZE SYSTEM" else "CONTINUE TO SYSTEM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Initialize",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun getIconForGroup(iconKey: String): ImageVector {
    return when (iconKey) {
        "mic" -> Icons.Default.Mic
        "person" -> Icons.Default.People
        "folder" -> Icons.Default.Folder
        "sms" -> Icons.Default.Sms
        "youtube" -> Icons.Default.PlayCircle
        "calendar" -> Icons.Default.CalendarToday
        "camera" -> Icons.Default.CameraAlt
        "notifications" -> Icons.Default.Notifications
        else -> Icons.Default.Security
    }
}
