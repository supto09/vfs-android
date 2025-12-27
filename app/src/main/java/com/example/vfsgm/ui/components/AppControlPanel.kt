package com.example.vfsgm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.DataState
import com.example.vfsgm.data.dto.JobState
import com.example.vfsgm.ui.components.atomics.SolidButton
import com.example.vfsgm.ui.components.atomics.SolidButtonVariant

@Composable
fun AppControlPanel(
    dataState: DataState,
    onAction: (AppControlAction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SolidButton(
            onClick = { onAction(AppControlAction.LoadApplicants) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load Applicants")
        }

        SolidButton(
            onClick = { onAction(AppControlAction.AddApplicants) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add Applicants")
        }

        SolidButton(
            onClick = { onAction(AppControlAction.LoadCalendar) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Load Calender")
        }


        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SolidButton(
                onClick = { onAction(AppControlAction.StartCheckIsSlotAvailable) },
                modifier = Modifier.weight(2f),
                enabled = dataState.checkSlotJobRunning !== JobState.IN_PROGRESS
            ) {
                Text("Check Slot")
            }

            SolidButton(
                onClick = { onAction(AppControlAction.StopCheckIsSlotAvailable) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }


        SolidButton(
            onClick = { onAction(AppControlAction.Logout) },
            modifier = Modifier.fillMaxWidth(),
            variant = SolidButtonVariant.Danger
        ) {
            Text("Logout")
        }
    }
}

sealed interface AppControlAction {
    data object LoadApplicants : AppControlAction
    data object AddApplicants : AppControlAction
    data object LoadCalendar : AppControlAction
    data object Logout : AppControlAction
    data object StartCheckIsSlotAvailable : AppControlAction
    data object StopCheckIsSlotAvailable : AppControlAction
}