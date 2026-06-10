package com.supermarket.app.ui
import com.supermarket.app.ui.smOutlinedColors

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
// force rebuild

@Composable
fun smChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = SMColors.Primary,
    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
    selectedLeadingIconColor = androidx.compose.ui.graphics.Color.White
)
