package com.supermarket.app.ui

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import com.supermarket.app.ui.theme.SMColors

@Composable
fun smOutlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SMColors.Primary,
    unfocusedBorderColor = SMColors.BgCardBorder,
    focusedLabelColor = SMColors.Primary,
    unfocusedLabelColor = SMColors.TextSecondary,
    focusedTextColor = SMColors.TextPrimary,
    unfocusedTextColor = SMColors.TextPrimary,
    focusedContainerColor = SMColors.BgCard,
    unfocusedContainerColor = SMColors.BgSurface
)

@Composable
fun smChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = SMColors.Primary,
    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
    selectedLeadingIconColor = androidx.compose.ui.graphics.Color.White
)
