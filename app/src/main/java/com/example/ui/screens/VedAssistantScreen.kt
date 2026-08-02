package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.UtilityService
import com.example.services.VoiceService
import com.example.ui.theme.VedraCyanAccent
import com.example.ui.theme.VedraTextMuted
import kotlinx.coroutines.launch

data class VedChatMessage(
    val id: String,
    val sender: String, // "user" or "vedra"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun VedAssistantScreen(
    dbService: DatabaseService,
    voiceService: VoiceService,
    onActivateVoice: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                VedChatMessage(
                    id = "msg_welcome",
                    sender = "vedra",
                    content = "Greetings! I am VEDRA, your personal AI Assistant. How can I empower your day today?"
                )
            )
        )
    }
    var isProcessing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Pulsing animation for AI orb
    val infiniteTransition = rememberInfiniteTransition(label = "VedOrbPulse")
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
            .padding(top = 8.dp)
    ) {
        // TOP HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, Color(0x25FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                com.example.ui.components.VedMathLogoIconCard(
                    size = 36.dp,
                    animated = true,
                    showBrandText = false
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "VEDRA AI",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Personal Intelligence System",
                        color = VedraTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Voice Mode Toggle Button
            IconButton(
                onClick = onActivateVoice,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7C3AED), Color(0xFF06B6D4))
                        )
                    )
                    .border(1.dp, Color(0xFFA78BFA), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Voice Mode",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // HERO AI ORB SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(orbScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.45f),
                                Color(0xFF06B6D4).copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFEC4899), Color(0xFF8B5CF6))
                        ),
                        shape = CircleShape
                    )
                    .clickable { onActivateVoice() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Orb",
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TAP VOICE",
                        color = Color(0xFFE0E7FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // QUICK PROMPT SUGGESTION CHIPS
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "⚡ Quick System Check",
                "📚 Summarize My Notes",
                "📇 Scan & Explain",
                "📂 Search VEDrive",
                "🎙️ Record Lecture",
                "⏰ Set Reminder"
            )
            items(suggestions) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1B182B))
                        .border(1.dp, Color(0xFF3B2D54), RoundedCornerShape(16.dp))
                        .clickable {
                            inputQuery = prompt.substringAfter(" ").trim()
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = prompt,
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // CHAT MESSAGES STREAM
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 40.dp, max = 290.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .background(
                                if (isUser) Brush.horizontalGradient(
                                    listOf(Color(0xFF6D28D9), Color(0xFF4C1D95))
                                ) else Brush.horizontalGradient(
                                    listOf(Color(0xFF1E1A2E), Color(0xFF181524))
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = if (isUser) Color(0xFFA78BFA) else Color(0xFF372A4D),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.ChatBubbleOutline else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isUser) Color(0xFFDDD6FE) else VedraCyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isUser) "You" else "VEDRA",
                                    color = if (isUser) Color(0xFFDDD6FE) else VedraCyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.content,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
            if (isProcessing) {
                item {
                    Text(
                        text = "VEDRA is thinking...",
                        color = VedraTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // FIXED QUERY INPUT BAR AT BOTTOM
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = {
                    Text(
                        text = "Ask VEDRA anything...",
                        color = VedraTextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ved_assistant_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF14121F),
                    unfocusedContainerColor = Color(0xFF14121F),
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF2E2744),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 3,
                trailingIcon = {
                    IconButton(
                        onClick = onActivateVoice,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = VedraCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        val text = inputQuery
                        inputQuery = ""
                        val userMsg = VedChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            sender = "user",
                            content = text
                        )
                        chatMessages = chatMessages + userMsg
                        isProcessing = true

                        scope.launch {
                            val localResult = UtilityService.parseAndExecuteLocalCommand(context, dbService, text)
                            val responseText = if (localResult.isHandled && localResult.responseMessage.isNotBlank()) {
                                localResult.responseMessage
                            } else {
                                com.example.services.GeminiService.generateResponse(
                                    prompt = text,
                                    dbService = dbService,
                                    context = context
                                )
                            }
                            val vedMsg = VedChatMessage(
                                id = "msg_${System.currentTimeMillis() + 1}",
                                sender = "vedra",
                                content = responseText
                            )
                            chatMessages = chatMessages + vedMsg
                            isProcessing = false

                            // Persist to database history
                            dbService.saveChatHistory("Ved Assistant Query", text, responseText)
                        }
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
                        )
                    )
                    .testTag("ved_assistant_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
