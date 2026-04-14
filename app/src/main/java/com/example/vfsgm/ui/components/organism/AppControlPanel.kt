package com.example.vfsgm.ui.components.organism

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
import com.example.vfsgm.ui.components.atomics.MySolidButton
import com.example.vfsgm.ui.components.atomics.SolidButtonVariant

@Composable
fun AppControlPanel(
    dataState: DataState,
    onAction: (AppControlAction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MySolidButton(
                onClick = { onAction(AppControlAction.LoadApplicants) },
                modifier = Modifier.weight(2f),
                enabled = dataState.loadApplicantsJobRunning != JobState.IN_PROGRESS
            ) {
                Text("Load Applicants")
            }
            MySolidButton(
                onClick = { onAction(AppControlAction.StopLoadApplicants) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MySolidButton(
                onClick = { onAction(AppControlAction.AddApplicants) },
                modifier = Modifier.weight(2f),
                enabled = dataState.addApplicantJobRunning != JobState.IN_PROGRESS
            ) {
                Text("Add Applicants")
            }
            MySolidButton(
                onClick = { onAction(AppControlAction.StopAddApplicants) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MySolidButton(
                onClick = { onAction(AppControlAction.StartCheckIsSlotAvailable) },
                modifier = Modifier.weight(2f),
                enabled = dataState.checkSlotJobRunning !== JobState.IN_PROGRESS
            ) {
                Text("Check Slot Ava")
            }

            MySolidButton(
                onClick = { onAction(AppControlAction.StopCheckIsSlotAvailable) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MySolidButton(
                onClick = { onAction(AppControlAction.LoadCalendar) },
                modifier = Modifier.weight(2f),
                enabled = dataState.loadCalenderJobRunning != JobState.IN_PROGRESS
            ) {
                Text("Load Calender")
            }
            MySolidButton(
                onClick = { onAction(AppControlAction.StopLoadCalender) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MySolidButton(
                onClick = { onAction(AppControlAction.LoadSlot) },
                modifier = Modifier.weight(2f),
                enabled = dataState.loadSlotJobRunning != JobState.IN_PROGRESS &&
                    dataState.scheduleJobRunning != JobState.IN_PROGRESS
            ) {
                Text("Load Slot")
            }

            MySolidButton(
                onClick = { onAction(AppControlAction.StopLoadSlot) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }

        MySolidButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = dataState.scheduleJobRunning != JobState.IN_PROGRESS
        ) {
            Text("Schedule")
        }


        MySolidButton(
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
    data object StopLoadApplicants : AppControlAction
    data object AddApplicants : AppControlAction
    data object StopAddApplicants : AppControlAction
    data object StartCheckIsSlotAvailable : AppControlAction
    data object StopCheckIsSlotAvailable : AppControlAction
    data object LoadCalendar : AppControlAction
    data object StopLoadCalender : AppControlAction
    data object LoadSlot : AppControlAction
    data object StopLoadSlot : AppControlAction
    data object Logout : AppControlAction
}
