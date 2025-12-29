package com.example.vfsgm.ui.components.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.data.dto.DataState
import com.example.vfsgm.data.dto.JobState
import com.example.vfsgm.ui.components.CloudflareModalWrapper
import com.example.vfsgm.ui.components.SettingsBottomSheet
import com.example.vfsgm.ui.components.TurnstileWebviewModal
import com.example.vfsgm.ui.components.atomics.MySolidButton

@Composable
fun SystemControlPanel(
    appConfig: AppConfig,
    dataState: DataState,
    onAction: (SystemControlAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) { CloudflareModalWrapper() }
            Box(modifier = Modifier.weight(1f)) { TurnstileWebviewModal() }
            Box {
                SettingsBottomSheet(
                    appConfig = appConfig,
                    onAppConfigChangeRequested = { newAppConfig ->
                        onAction(
                            SystemControlAction.AppConfigChangeRequest(
                                appConfig = newAppConfig
                            )
                        )
                    }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MySolidButton(
                onClick = { onAction(SystemControlAction.StartReLogin) },
                enabled = dataState.reLoginJobRunning != JobState.IN_PROGRESS,
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    "Start ReLogin"
                )
            }
            MySolidButton(
                onClick = { onAction(SystemControlAction.StopReLogin) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Stop"
                )
            }
        }
    }

}

sealed interface SystemControlAction {
    data class AppConfigChangeRequest(val appConfig: AppConfig) : SystemControlAction
    object StartReLogin : SystemControlAction
    object StopReLogin : SystemControlAction
}