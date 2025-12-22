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
    onLoadApplicant: () -> Unit,
    onAddApplicant: () -> Unit,
    onGenderLoad: () -> Unit,
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SolidButton(
            onClick = onLoadApplicant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load Applicants")
        }

        SolidButton(
            onClick = onAddApplicant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add Applicants")
        }

        SolidButton(
            onClick = onGenderLoad,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Get Gender")
        }

        SolidButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            variant = SolidButtonVariant.Danger
        ) {
            Text("Logout")
        }
    }
}