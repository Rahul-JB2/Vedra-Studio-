#!/bin/bash
cat << 'IMPORTS' > temp.kt
package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VedraSurface
import com.example.ui.theme.VedraBorder
import com.example.ui.components.CustomModal

IMPORTS
cat app/src/main/java/com/example/ui/screens/settings/SettingsComponents.kt >> temp.kt
mv temp.kt app/src/main/java/com/example/ui/screens/settings/SettingsComponents.kt
