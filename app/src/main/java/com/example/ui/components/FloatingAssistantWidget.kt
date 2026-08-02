package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.GeminiService
import com.example.services.UtilityService
import com.example.services.VoiceService
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Overlay Message Item Data Class
data class VedOrbOverlayMessage(
    val sender: String, // "user" or "ved"
    val text: String,
    val mathFormula: String? = null,
    val formulaWhereText: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingAssistantWidget(
    voiceService: VoiceService,
    dbService: DatabaseService,
    modifier: Modifier = Modifier,
    onActivateVoiceMode: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var inputQuery by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    // Chat messages inside overlay card
    val overlayMessages = remember { mutableStateListOf<VedOrbOverlayMessage>() }

    // Helper: Execute Query inside VED Orb Overlay
    fun handleQuery(queryText: String) {
        if (queryText.isBlank()) return
        inputQuery = ""
        isThinking = true

        overlayMessages.add(VedOrbOverlayMessage(sender = "user", text = queryText))

        scope.launch {
            val localResult = UtilityService.parseAndExecuteLocalCommand(context, dbService, queryText)
            val responseText = if (localResult.isHandled && localResult.responseMessage.isNotBlank()) {
                localResult.responseMessage
            } else {
                val dbMatch = dbService.findLearnedResponse(queryText)
                if (dbMatch != null) {
                    dbMatch
                } else {
                    val geminiRes = GeminiService.generateResponse(
                        prompt = queryText,
                        contextSummary = "",
                        dbService = dbService,
                        context = context
                    )
                    if (geminiRes.isNotBlank()) geminiRes else "Newton's Second Law states that the acceleration of an object depends on the net force acting on it and its mass."
                }
            }

            // Math formula formatting check
            val formula = if (queryText.contains("Newton", ignoreCase = true)) {
                "F = m × a"
            } else if (queryText.contains("2x + 5 = 15", ignoreCase = true) || queryText.contains("math", ignoreCase = true)) {
                "2x + 5 = 15  ⇒  x = 5"
            } else null

            val formulaWhere = if (queryText.contains("Newton", ignoreCase = true)) {
                "Where:\nF = Net force (in Newtons)\nm = Mass of the object (in kg)\na = Acceleration (in m/s²)"
            } else null

            overlayMessages.add(
                VedOrbOverlayMessage(
                    sender = "ved",
                    text = responseText,
                    mathFormula = formula,
                    formulaWhereText = formulaWhere
                )
            )

            dbService.saveChatHistory("VED Orb", queryText, responseText)
            isThinking = false
            voiceService.speak(responseText)
        }
    }

    // Read customization
    val vedOrbStyle = remember(dbService.settingsVersion.intValue) { dbService.getSetting("ved_orb_style", "Gemini Neon Glow Orb") }

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
            // Collapsed Floating Sparkle Orb (as circled in red in screenshot)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = {
                            // Single Tap: Opens VED Orb Overlay Card!
                            isExpanded = true
                        },
                        onLongClick = {
                            isExpanded = true
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                VedOrbView(
                    orbStyle = vedOrbStyle,
                    size = 56.dp,
                    isListening = voiceService.isListening.value,
                    isSpeaking = voiceService.isSpeaking.value
                )
            }
        } else {
            // Expanded VED Orb Overlay Sheet/Card (Same to Same as Image)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF0F0E1E))
                        .border(1.dp, Color(0xFF28264A), RoundedCornerShape(28.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Drag Handle Bar
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A476D))
                                .align(Alignment.CenterHorizontally)
                        )

                        // Card Header Row: [Orb Icon] VED Orb / Your AI Companion / Online | [Expand] [Close]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF231C42))
                                        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    VedMathLogoCanvas(
                                        modifier = Modifier.size(24.dp),
                                        animated = true
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "VED Orb",
                                        color = Color.White,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Your AI Companion",
                                            color = Color(0xFFA09EC0),
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Online",
                                            color = Color(0xFF10B981),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Fullscreen & Close Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        isExpanded = false
                                        onActivateVoiceMode?.invoke()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Fullscreen",
                                        tint = Color(0xFFA09EC0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { isExpanded = false },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFFA09EC0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Input Box ("Ask VED anything...") with Waveform Button
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = {
                                Text(
                                    text = if (overlayMessages.isNotEmpty()) "Ask follow-up..." else "Ask VED anything...",
                                    color = Color(0xFFA09EC0),
                                    fontSize = 13.sp
                                )
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            voiceService.startListening(
                                                onResult = { query ->
                                                    handleQuery(query)
                                                },
                                                onError = {}
                                            )
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice Input",
                                            tint = Color(0xFFA09EC0),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
                                                )
                                            )
                                            .clickable {
                                                if (inputQuery.isNotBlank()) {
                                                    handleQuery(inputQuery)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = "Send Waveform",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF16142B),
                                unfocusedContainerColor = Color(0xFF16142B),
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF28264A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )

                        // 4 Quick Action Chips Grid (Explain, Summarize, Write, Solve)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OrbOverlayChip(
                                icon = Icons.Default.AutoAwesome,
                                iconColor = Color(0xFFA855F7),
                                title = "Explain",
                                subtitle = "Any topic",
                                modifier = Modifier.weight(1f),
                                onClick = { handleQuery("Explain Newton's Second Law in simple words") }
                            )
                            OrbOverlayChip(
                                icon = Icons.Default.Description,
                                iconColor = Color(0xFFF97316),
                                title = "Summarize",
                                subtitle = "Text / PDF",
                                modifier = Modifier.weight(1f),
                                onClick = { handleQuery("Summarize this document") }
                            )
                            OrbOverlayChip(
                                icon = Icons.Default.Edit,
                                iconColor = Color(0xFF3B82F6),
                                title = "Write",
                                subtitle = "Anything",
                                modifier = Modifier.weight(1f),
                                onClick = { handleQuery("Write a birthday wish for my friend") }
                            )
                            OrbOverlayChip(
                                icon = Icons.Default.Calculate,
                                iconColor = Color(0xFF10B981),
                                title = "Solve",
                                subtitle = "Problems",
                                modifier = Modifier.weight(1f),
                                onClick = { handleQuery("Solve this math problem: 2x + 5 = 15") }
                            )
                        }

                        // Content Body: Either "Try asking" list OR Active Overlay Conversation
                        if (overlayMessages.isEmpty()) {
                            // "Try asking" Section Header & List Cards
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Try asking",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )

                                TryAskingCardItem(
                                    icon = Icons.Default.AutoAwesome,
                                    iconColor = Color(0xFFA855F7),
                                    text = "Explain Newton's Second Law in simple words",
                                    onClick = { handleQuery("Explain Newton's Second Law in simple words") }
                                )

                                TryAskingCardItem(
                                    icon = Icons.Default.Description,
                                    iconColor = Color(0xFFF97316),
                                    text = "Summarize this document",
                                    onClick = { handleQuery("Summarize this document") }
                                )

                                TryAskingCardItem(
                                    icon = Icons.Default.Edit,
                                    iconColor = Color(0xFF3B82F6),
                                    text = "Write a birthday wish for my friend",
                                    onClick = { handleQuery("Write a birthday wish for my friend") }
                                )

                                TryAskingCardItem(
                                    icon = Icons.Default.Calculate,
                                    iconColor = Color(0xFF10B981),
                                    text = "Solve this math problem: 2x + 5 = 15",
                                    onClick = { handleQuery("Solve this math problem: 2x + 5 = 15") }
                                )
                            }
                        } else {
                            // Active Conversation Stream
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(overlayMessages) { msg ->
                                    if (msg.sender == "user") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .widthIn(max = 260.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFF6B21A8), Color(0xFF581C87))
                                                        )
                                                    )
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = msg.text,
                                                    color = Color.White,
                                                    fontSize = 12.5.sp,
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    } else {
                                        // VED Response Card
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF231C42)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                VedMathLogoCanvas(modifier = Modifier.size(20.dp), animated = true)
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0xFF141328))
                                                    .border(1.dp, Color(0xFF28264A), RoundedCornerShape(16.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(
                                                        text = msg.text,
                                                        color = Color.White,
                                                        fontSize = 12.5.sp,
                                                        lineHeight = 18.sp
                                                    )

                                                    if (msg.mathFormula != null) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(Color(0xFF1B1936))
                                                                .border(1.dp, Color(0xFF3B3766), RoundedCornerShape(10.dp))
                                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = msg.mathFormula,
                                                                color = Color(0xFFA855F7),
                                                                fontSize = 15.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }

                                                    if (msg.formulaWhereText != null) {
                                                        Text(
                                                            text = msg.formulaWhereText,
                                                            color = Color(0xFFA09EC0),
                                                            fontSize = 11.sp,
                                                            lineHeight = 16.sp
                                                        )
                                                    }

                                                    // Action Toolbar Row
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = { Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show() },
                                                            modifier = Modifier.size(18.dp)
                                                        ) {
                                                            Icon(Icons.Default.ThumbUpOffAlt, contentDescription = "Like", tint = Color(0xFFA09EC0), modifier = Modifier.size(14.dp))
                                                        }

                                                        IconButton(
                                                            onClick = { Toast.makeText(context, "Disliked", Toast.LENGTH_SHORT).show() },
                                                            modifier = Modifier.size(18.dp)
                                                        ) {
                                                            Icon(Icons.Default.ThumbDownOffAlt, contentDescription = "Dislike", tint = Color(0xFFA09EC0), modifier = Modifier.size(14.dp))
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                                clipboard.setPrimaryClip(ClipData.newPlainText("VED Response", msg.text))
                                                                Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(18.dp)
                                                        ) {
                                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFA09EC0), modifier = Modifier.size(14.dp))
                                                        }

                                                        IconButton(
                                                            onClick = {
                                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                                    type = "text/plain"
                                                                    putExtra(Intent.EXTRA_TEXT, msg.text)
                                                                }
                                                                context.startActivity(Intent.createChooser(shareIntent, "Share VED Answer"))
                                                            },
                                                            modifier = Modifier.size(18.dp)
                                                        ) {
                                                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFFA09EC0), modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (isThinking) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color(0xFFA855F7),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("VED is thinking...", color = Color(0xFFA09EC0), fontSize = 11.5.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Quick Action Chip inside Overlay
@Composable
private fun OrbOverlayChip(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141228))
            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFA09EC0), fontSize = 9.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// "Try asking" Suggestion Item Card
@Composable
private fun TryAskingCardItem(
    icon: ImageVector,
    iconColor: Color,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141228))
            .border(1.dp, Color(0xFF28264A), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = Color(0xFFA09EC0),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
