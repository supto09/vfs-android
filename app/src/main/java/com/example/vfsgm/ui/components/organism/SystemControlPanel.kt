package com.example.vfsgm.ui.components.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.data.dto.DataState
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.data.dto.JobState
import com.example.vfsgm.ui.components.CloudflareModalWrapper
import com.example.vfsgm.ui.components.SettingsBottomSheet
import com.example.vfsgm.ui.components.TurnstileWebviewModal
import com.example.vfsgm.ui.components.atomics.MySolidButton
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SystemControlPanel(
    appConfig: AppConfig,
    entry: Entry,
    dataState: DataState,
    onAction: (SystemControlAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = "Device Index",
                modifier = Modifier.size(15.dp),
                tint = androidx.compose.ui.graphics.Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = appConfig.deviceIndex.toString(),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = androidx.compose.ui.graphics.Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = "Entry Index",
                modifier = Modifier.size(15.dp),
                tint = androidx.compose.ui.graphics.Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = appConfig.entryIndex.toString(),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = androidx.compose.ui.graphics.Color.White
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) { CloudflareModalWrapper(deviceIndex = appConfig.deviceIndex) }
            Box(modifier = Modifier.weight(1f)) { TurnstileWebviewModal(deviceIndex = appConfig.deviceIndex) }
            Box {
                SettingsBottomSheet(
                    appConfig = appConfig,
                    entry = entry,
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
