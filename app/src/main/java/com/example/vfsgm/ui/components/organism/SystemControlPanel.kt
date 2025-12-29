package com.example.vfsgm.ui.components.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.ui.components.CloudflareModalWrapper
import com.example.vfsgm.ui.components.SettingsBottomSheet
import com.example.vfsgm.ui.components.TurnstileWebviewModal

@Composable
fun SystemControlPanel(
    appConfig: AppConfig,
    onAction: (SystemControlAction) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) { CloudflareModalWrapper() }
        Box(modifier = Modifier.weight(1f)) { TurnstileWebviewModal() }
        Box {
            SettingsBottomSheet(
                appConfig = appConfig,
                onAppConfigChangeRequested = { newAppConfig->
                    onAction(
                        SystemControlAction.AppConfigChangeRequest(
                            appConfig = newAppConfig
                        )
                    )
                }
            )
        }
    }
}

sealed interface SystemControlAction {
    data class AppConfigChangeRequest(val appConfig: AppConfig) : SystemControlAction
}