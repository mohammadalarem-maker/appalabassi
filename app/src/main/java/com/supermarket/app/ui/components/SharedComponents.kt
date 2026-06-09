package com.supermarket.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supermarket.app.ui.theme.SMColors

// ============================
// SHARED FIELD COMPONENT
// ============================
@Composable
fun SMField(
    label: String,
    value: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = {
            Icon(icon, null, tint = SMColors.TextSecondary, modifier = Modifier.size(20.dp))
        },
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = smOutlinedFieldColors(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun smOutlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = SMColors.Primary,
    unfocusedBorderColor    = SMColors.BgCardBorder,
    focusedLabelColor       = SMColors.Primary,
    unfocusedLabelColor     = SMColors.TextSecondary,
    cursorColor             = SMColors.Primary,
    focusedTextColor        = SMColors.TextPrimary,
    unfocusedTextColor      = SMColors.TextPrimary,
    focusedContainerColor   = SMColors.BgCard,
    unfocusedContainerColor = SMColors.BgSurface
)

// ============================
// SECTION CARD
// ============================
@Composable
fun SMSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SMColors.BgCard),
        border = BorderStroke(1.dp, SMColors.BgCardBorder)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = SMColors.Primary, modifier = Modifier.size(18.dp))
                Text(
                    title, color = SMColors.Primary,
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                )
            }
            content()
        }
    }
}

// ============================
// STAT PILL
// ============================
@Composable
fun StatPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = color.copy(0.1f)),
        border   = BorderStroke(1.dp, color.copy(0.25f))
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(label, color = SMColors.TextSecondary, fontSize = 10.sp)
        }
    }
}

// ============================
// EMPTY STATE
// ============================
@Composable
fun EmptyState(emoji: String, message: String) {
    Box(
        Modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(message, color = SMColors.TextMuted, fontSize = 15.sp)
        }
    }
}
