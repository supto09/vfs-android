package com.example.vfsgm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.Applicant
import com.example.vfsgm.ui.components.atomics.SolidButton
import com.example.vfsgm.ui.components.atomics.SolidButtonVariant

@Composable
fun AppControlPanel(
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

        SolidButton(
            onClick = { onAction(AppControlAction.CheckIsSlotAvailable) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Check Is Slot Available")
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
    data object CheckIsSlotAvailable : AppControlAction
}