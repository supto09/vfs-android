package com.example.vfsgm.ui.components.atomics


import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

enum class SolidButtonVariant {
    Regular,
    Danger,
    Warning;

    fun containerColor(colors: ColorScheme): Color =
        when (this) {
            Regular -> colors.primary
            Danger  -> colors.error
            Warning -> colors.tertiary
        }

    fun contentColor(colors: ColorScheme): Color =
        when (this) {
            Regular -> colors.onPrimary
            Danger  -> colors.onError
            Warning -> colors.onTertiary
        }
}

@Composable
fun MySolidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SolidButtonVariant = SolidButtonVariant.Regular,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp), // 👈 global button shape
        colors = ButtonDefaults.buttonColors(
            containerColor = variant.containerColor(colors),
            contentColor = variant.contentColor(colors),
            disabledContainerColor = colors.surfaceVariant,
            disabledContentColor = colors.onSurfaceVariant
        ),
        content = content
    )
}