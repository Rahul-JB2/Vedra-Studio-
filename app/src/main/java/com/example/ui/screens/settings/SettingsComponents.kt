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
import com.example.ui.theme.*
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ChevronRight

import com.example.ui.components.CustomModal

@Composable
fun PreferenceSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFA78BFA),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
}

@Composable
fun PreferenceCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VedraSurface)
            .border(1.dp, VedraBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
fun PreferenceIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VedraSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VedraPurplePrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DropdownPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VedraSurfaceVariant)
            .border(1.dp, VedraBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                color = VedraTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = VedraTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SegmentedToggleTwo(
    option1: String,
    option2: String,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VedraSurfaceVariant)
            .border(1.dp, VedraBorder, RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isOpt1Selected = selectedOption == option1
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOpt1Selected) VedraPurplePrimary else Color.Transparent)
                    .clickable { onSelect(option1) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option1,
                    color = if (isOpt1Selected) Color.White else VedraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isOpt1Selected) FontWeight.Bold else FontWeight.Medium
                )
            }

            val isOpt2Selected = selectedOption == option2
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOpt2Selected) VedraPurplePrimary else Color.Transparent)
                    .clickable { onSelect(option2) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option2,
                    color = if (isOpt2Selected) Color.White else VedraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isOpt2Selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) VedraSurfaceVariant else VedraSurface)
            .border(
                1.dp,
                if (isSelected) VedraPurplePrimary else VedraBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) VedraPurplePrimary else VedraTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = VedraTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = VedraTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(VedraPurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, VedraTextMuted, CircleShape)
                )
            }
        }
    }
}

@Composable
fun OtherPreferenceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            PreferenceIconBox(icon = icon)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = VedraTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = VedraTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = Color(0xFFD1D5DB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SelectionListModal(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    CustomModal(
        visible = true,
        title = title,
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSel = option == selectedOption
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) Color(0xFF231B38) else Color(0xFF141122))
                        .border(1.dp, if (isSel) Color(0xFF7C3AED) else Color(0xFF201B30), RoundedCornerShape(12.dp))
                        .clickable { onSelect(option) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            color = if (isSel) Color(0xFFC4B5FD) else Color.White,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (isSel) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VedOrbStyleModal(
    selectedStyle: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val styles = listOf(
        "Mathematical Waveform Logo (VED)" to "Gaussian modulated sine wave W(x) = ∑ A_n sin(ω_n x) e^(-(x/σ_n)²) exact brand logo equation",
        "Gemini Neon Glow Orb" to "Multi-color rotating gradient aura & pulsing core (Inspired by Gemini Live)",
        "Google Voice Waveform Orb" to "Vibrant Google colors red, blue, yellow, green waveform & dots (Inspired by Google Assistant)",
        "VED Purple Energy Orb" to "Classic cosmic purple pulsing core with soundwave aura",
        "Quantum Hologram Orb" to "Cyberpunk cyan holographic particle aura"
    )

    CustomModal(
        visible = true,
        title = "Select VED Orb Style",
        onDismissRequest = onDismiss
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            styles.forEach { (styleName, description) ->
                val isSel = styleName == selectedStyle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSel) Color(0xFF2E1B4E) else Color(0xFF141122))
                        .border(1.5.dp, if (isSel) Color(0xFFA78BFA) else Color(0xFF201B30), RoundedCornerShape(14.dp))
                        .clickable { onSelect(styleName) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.example.ui.components.VedOrbView(
                            orbStyle = styleName,
                            size = 46.dp,
                            isListening = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = styleName,
                                color = if (isSel) Color.White else Color(0xFFE5E7EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = description,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        if (isSel) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
