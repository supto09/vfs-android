package com.example.vfsgm.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vfsgm.ui.components.AppControlAction
import com.example.vfsgm.ui.components.AppControlPanel
import com.example.vfsgm.ui.components.AuthControlPanel
import com.example.vfsgm.ui.components.CloudflareModalWrapper
import com.example.vfsgm.ui.components.TurnstileWebviewModal
import com.example.vfsgm.viewmodel.MainViewModel

@Composable
fun AppScreen(viewModel: MainViewModel = viewModel()) {
    val sessionState by viewModel.sessionState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CloudflareModalWrapper()

        TurnstileWebviewModal()

        Text(
            text = dataState.toString()
        )



        AnimatedContent(
            targetState = sessionState.accessToken,
            label = "AuthStateTransition"
        ) { accessToken ->
            when (accessToken.isNullOrEmpty()) {
                true -> AuthControlPanel(
                    onLoginClick = viewModel::login
                )

                false -> AppControlPanel(
                    onAction = { action ->
                        when (action) {
                            AppControlAction.LoadApplicants -> viewModel.loadApplicants()
                            AppControlAction.AddApplicants -> viewModel.addApplicant()
                            AppControlAction.LoadCalendar -> viewModel.loadCalender()
                            AppControlAction.Logout -> viewModel.logout()
                            AppControlAction.CheckIsSlotAvailable -> viewModel.checkIsSlotAvailable()
                        }
                    }
                )
            }
        }
    }
}