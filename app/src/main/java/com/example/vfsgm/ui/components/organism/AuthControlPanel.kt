package com.example.vfsgm.ui.components.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.DataState
import com.example.vfsgm.data.dto.JobState
import com.example.vfsgm.ui.components.atomics.MySolidButton

@Composable
fun AuthControlPanel(
    dataState: DataState,
    onLoginClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val loginInProgress = dataState.loginJobRunning == JobState.IN_PROGRESS
    val verifyInProgress = dataState.verifyOtpJobRunning == JobState.IN_PROGRESS

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MySolidButton(
                onClick = onLoginClick,
                modifier = Modifier.weight(2f),
                enabled = !loginInProgress && !verifyInProgress
            ) {
                Text("Login")
            }

            MySolidButton(
                onClick = onStopClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Stop")
            }
        }
    }
}
