package com.example.vfsgm.ui.components.atomics


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color

enum class OutlinedButtonVariant {
    Regular,
    Danger,
    Warning;

    fun contentColor(colors: ColorScheme): Color =
        when (this) {
            Regular -> colors.primary
            Danger -> colors.error
            Warning -> colors.tertiary
        }

    fun borderColor(colors: ColorScheme): Color =
        when (this) {
            Regular -> colors.primary
            Danger -> colors.error
            Warning -> colors.tertiary
        }
}

@Composable
fun MyOutLinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OutlinedButtonVariant = OutlinedButtonVariant.Regular,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            width = 1.dp,
            color = variant.borderColor(colors)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = variant.contentColor(colors),
            disabledContentColor = colors.onSurfaceVariant
        ),
        content = content
    )
}