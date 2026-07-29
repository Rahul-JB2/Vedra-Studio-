package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VedraBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background

val VedraSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val VedraSurfaceVariant: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val VedraBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

val VedraPurplePrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary

val VedraPurpleSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.secondary

val VedraCyanAccent = Color(0xFF06B6D4)
val VedraBlueAccent = Color(0xFF3B82F6)
val VedraPinkAccent = Color(0xFFEC4899)

val VedraTextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onBackground

val VedraTextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val VedraTextMuted: Color
    @Composable get() = MaterialTheme.colorScheme.outline

val VedraCardGlow = Color(0x338B5CF6)
val VedraOnlineGreen = Color(0xFF10B981)

