package com.example.ui.components

import com.example.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.ui.theme.Spacing
import com.example.ui.theme.VedraCyanAccent
import com.example.ui.theme.VedraPinkAccent
import com.example.ui.theme.VedraPurplePrimary
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraTextMuted
import com.example.ui.theme.VedraTextPrimary
import com.example.ui.theme.VedraTextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingAssistantWidget(
    voiceService: VoiceService,
    dbService: DatabaseService,
    modifier: Modifier = Modifier,
    onActivateVoiceMode: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Read customization from SQLite Settings
    val widgetSize = dbService.getSetting("widget_size", "Medium")
    val themeGlow = dbService.getSetting("widget_glow", "Neon Purple")
    val opacityStr = dbService.getSetting("widget_opacity", "1.0")
    val widgetOpacity = opacityStr.toFloatOrNull() ?: 1.0f

    val vedOrbStyle = remember { dbService.getSetting("ved_orb_style", "Gemini Neon Glow Orb") }

    val orbSize = when (widgetSize) {
        "Small" -> 44.dp
        "Large" -> 68.dp
        else -> 50.dp
    }

    val glowColor = when (themeGlow) {
        "Cyan" -> VedraCyanAccent
        "Minimal" -> Color.White.copy(alpha = 0.5f)
        else -> VedraPurplePrimary
    }

    var widgetResponseText by remember { mutableStateOf("Tap or say \"Ved\" to speak") }
    var textInputMode by remember { mutableStateOf(false) }
    var textInputQuery by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        if (!isExpanded) {
            // Collapsed Floating Orb
            Box(
                modifier = Modifier
                    .size(orbSize)
                    .alpha(widgetOpacity)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = {
                            // Single Tap: Open VED Voice Assistant Popup Box
                            onActivateVoiceMode?.invoke()
                        },
                        onDoubleClick = {
                            onActivateVoiceMode?.invoke()
                        },
                        onLongClick = {
                            // Long Press: Toggle Expanded Card
                            isExpanded = true
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                VedOrbView(
                    orbStyle = vedOrbStyle,
                    size = orbSize,
                    isListening = voiceService.isListening.value,
                    isSpeaking = voiceService.isSpeaking.value
                )
            }
        } else {
            // Expanded Card View
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(VedraSurface.copy(alpha = 0.95f))
                        .border(1.5.dp, glowColor, RoundedCornerShape(24.dp))
                        .padding(Spacing.medium)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        // Top Header: Purple "VEDRA" text and Close (X) icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "VEDRA Logo",
                                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(5.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VEDRA",
                                    color = VedraPurplePrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Status: Green dot with "Listening..." / "Online"
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (voiceService.isListening.value) Color(0xFF4CAF50) else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (voiceService.isListening.value) "Listening..." else "Online",
                                    color = if (voiceService.isListening.value) Color(0xFF4CAF50) else VedraTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            IconButton(onClick = { isExpanded = false }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Card",
                                    tint = VedraTextMuted
                                )
                            }
                        }

                        // Subtitle & Response Box
                        Text(
                            text = widgetResponseText,
                            color = VedraTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E2E))
                                .padding(10.dp)
                        )

                        Text(
                            text = "Tap or say \"Ved\"",
                            color = VedraTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // Quick Action Grid (4 Shortcut Squares)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            QuickShortcutSquare(
                                icon = Icons.Default.Apps,
                                label = "WhatsApp",
                                onClick = {
                                    val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, "open whatsapp")
                                    widgetResponseText = res.responseMessage
                                }
                            )
                            QuickShortcutSquare(
                                icon = Icons.Default.Apps,
                                label = "YouTube",
                                onClick = {
                                    val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, "open youtube")
                                    widgetResponseText = res.responseMessage
                                }
                            )
                            QuickShortcutSquare(
                                icon = Icons.Default.Calculate,
                                label = "Calculator",
                                onClick = {
                                    val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, "calculator 50 + 50")
                                    widgetResponseText = res.responseMessage
                                }
                            )
                            QuickShortcutSquare(
                                icon = Icons.Default.EditNote,
                                label = "Notes",
                                onClick = {
                                    val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, "take note Check floating widget")
                                    widgetResponseText = res.responseMessage
                                }
                            )
                        }

                        if (textInputMode) {
                            CustomInput(
                                value = textInputQuery,
                                onValueChange = { textInputQuery = it },
                                placeholder = "Type command & press enter..."
                            )
                            CustomButton(
                                text = "Submit Command",
                                onClick = {
                                    if (textInputQuery.isNotBlank()) {
                                        val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, textInputQuery)
                                        widgetResponseText = res.responseMessage
                                        textInputQuery = ""
                                        textInputMode = false
                                    }
                                },
                                modifier = Modifier.height(32.dp)
                            )
                        }

                        // Bottom Bar: Wide purple "Tap to talk" button and separate keyboard icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomButton(
                                text = if (voiceService.isListening.value) "Listening... (Tap to stop)" else "Tap to talk",
                                onClick = {
                                    if (voiceService.isListening.value) {
                                        voiceService.stopListening()
                                    } else {
                                        voiceService.startListening(
                                            onResult = { query ->
                                                val res = UtilityService.parseAndExecuteLocalCommand(context, dbService, query)
                                                widgetResponseText = res.responseMessage
                                                voiceService.speak(res.responseMessage)
                                            },
                                            onError = { err -> widgetResponseText = err }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f).height(38.dp)
                            )

                            IconButton(
                                onClick = { textInputMode = !textInputMode },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B293D))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "Switch to Keyboard",
                                    tint = VedraCyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickShortcutSquare(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF252438))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = VedraCyanAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = VedraTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
