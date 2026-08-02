package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.DatabaseService
import com.example.services.VoiceService
import com.example.ui.theme.VedraBackground

@Composable
fun SettingsScreen(dbService: DatabaseService, voiceService: VoiceService) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VedraBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Personalization & Preferences",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
        }
    }
}
