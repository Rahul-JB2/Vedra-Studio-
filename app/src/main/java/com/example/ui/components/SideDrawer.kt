package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VedraBorder
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary

data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val badge: String? = null,
    val actionKey: String
)

@Composable
fun SideDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    onSelectMenuItem: (String) -> Unit
) {
    val items = listOf(
        DrawerMenuItem("Database & Drive", Icons.Default.Folder, "AI Sync", "database"),
        DrawerMenuItem("Action", Icons.Default.FlashOn, "Coming soon", "action"),
        DrawerMenuItem("Automation", Icons.Default.AltRoute, "Coming soon", "automation"),
        DrawerMenuItem("Plugins", Icons.Default.Extension, "Coming soon", "plugins"),
        DrawerMenuItem("Custom Commands", Icons.Default.Code, "Coming soon", "custom_commands"),
        DrawerMenuItem("Notification", Icons.Default.Notifications, "Coming soon", "notification")
    )

    if (isOpen) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Semi-transparent dark backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { onClose() }
            )

            // Left drawer panel
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(290.dp)
                        .background(Color(0xFF0F0F1A))
                        .border(1.dp, VedraBorder)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        // Top Header: V Avatar + VEDRA title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E1A47))
                                    .border(1.5.dp, VedraPurplePrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "V",
                                    color = VedraPurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "VEDRA",
                                    color = VedraPurplePrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "AI Assistant",
                                    color = VedraTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(VedraBorder)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Menu Items List
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF1B1B2A))
                                        .border(1.dp, Color(0xFF2B2B3D), RoundedCornerShape(14.dp))
                                        .clickable {
                                            onSelectMenuItem(item.actionKey)
                                            onClose()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2B293D)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.title,
                                                    tint = VedraPurplePrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = item.title,
                                                color = VedraTextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (item.badge != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF2A223D))
                                                    .border(1.dp, VedraPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = item.badge,
                                                    color = Color(0xFFB095FF),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Footer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = VedraTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "More features arriving soon",
                            color = VedraTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
