package com.example.vfsgm.ui.components.atomics

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

enum class SolidButtonVariant {
    Regular,
    Danger,
    Warning;

    fun containerColor(colors: ColorScheme): Color =
        when (this) {
            Regular -> Color(0xFFE85D75)
            Danger  -> colors.error
            Warning -> Color(0xFFDE6A55)
        }

    fun contentColor(colors: ColorScheme): Color =
        when (this) {
            Regular -> Color(0xFFF8FBFF)
            Danger  -> colors.onError
            Warning -> Color(0xFFF2FFFE)
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
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = variant.containerColor(colors),
            contentColor = variant.contentColor(colors),
            disabledContainerColor = lerp(
                variant.containerColor(colors),
                Color.White,
                0.28f
            ).copy(alpha = 0.16f),
            disabledContentColor = variant.contentColor(colors).copy(alpha = 0.34f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        content = content
    )
}
