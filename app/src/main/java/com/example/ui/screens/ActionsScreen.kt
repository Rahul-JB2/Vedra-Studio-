package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CustomButton
import com.example.ui.components.CustomCard
import com.example.ui.components.CustomInput
import com.example.ui.components.CustomModal
import com.example.ui.theme.Spacing
import com.example.ui.theme.VedraBackground
import com.example.ui.theme.VedraBlueAccent
import com.example.ui.theme.VedraBorder
import com.example.ui.theme.VedraCyanAccent
import com.example.ui.theme.VedraPinkAccent
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraPurpleSecondary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val command: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun ActionsScreen(
    onExecuteAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isModalOpen by remember { mutableStateOf(false) }
    var newActionName by remember { mutableStateOf("") }
    var newActionCommand by remember { mutableStateOf("") }

    val customActions = remember {
        mutableStateListOf<QuickActionItem>()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VedraBackground)
            .padding(horizontal = Spacing.medium),
        contentPadding = PaddingValues(vertical = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        item {
            Text(
                text = "ACTIONS",
                color = VedraTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            CustomInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search actions...",
                leadingIcon = Icons.Default.Search
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK ACTIONS",
                    color = VedraTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                CustomButton(
                    text = "Create Action",
                    icon = Icons.Default.Add,
                    onClick = { isModalOpen = true },
                    modifier = Modifier.height(34.dp)
                )
            }
        }

        if (customActions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No custom quick actions created yet. Tap 'Create Action' to create one.",
                        color = VedraTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(customActions.filter { it.title.contains(searchQuery, ignoreCase = true) }) { action ->
                CustomCard(
                    onClick = { onExecuteAction(action.command) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(action.iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = action.iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.medium))
                            Column {
                                Text(
                                    text = action.title,
                                    color = VedraTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = action.subtitle,
                                    color = VedraTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        CustomButton(
                            text = "Run",
                            onClick = { onExecuteAction(action.command) },
                            isSecondary = true,
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "ALL CATEGORIES",
                color = VedraTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = Spacing.small)
            )
        }

        val categories = listOf(
            Triple("Communication", "Calls, SMS, Email", Icons.Default.Message),
            Triple("Productivity", "Calendar, Reminders", Icons.Default.Folder),
            Triple("Utility", "Calculator, Timer", Icons.Default.Settings),
            Triple("Media", "Music, Playlists", Icons.Default.MusicNote)
        )

        items(categories) { (cat, sub, icon) ->
            CustomCard(
                onClick = { onExecuteAction(cat) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = VedraPurpleSecondary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Column {
                        Text(text = cat, color = VedraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = sub, color = VedraTextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Modal using CustomModal
    CustomModal(
        visible = isModalOpen,
        title = "Create Custom Action",
        onDismissRequest = { isModalOpen = false }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            CustomInput(
                value = newActionName,
                onValueChange = { newActionName = it },
                placeholder = "Action Name (e.g. Study Mode)"
            )
            CustomInput(
                value = newActionCommand,
                onValueChange = { newActionCommand = it },
                placeholder = "Command (e.g. open youtube)"
            )
            CustomButton(
                text = "Save Action",
                onClick = {
                    if (newActionName.isNotBlank() && newActionCommand.isNotBlank()) {
                        customActions.add(
                            QuickActionItem(
                                title = newActionName,
                                subtitle = newActionCommand,
                                command = newActionCommand,
                                icon = Icons.Default.Add,
                                iconColor = VedraCyanAccent
                            )
                        )
                        newActionName = ""
                        newActionCommand = ""
                        isModalOpen = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
