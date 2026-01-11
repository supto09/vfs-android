package com.example.vfsgm.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vfsgm.ui.components.organism.AppControlAction
import com.example.vfsgm.ui.components.organism.AppControlPanel
import com.example.vfsgm.ui.components.organism.AuthControlPanel
import com.example.vfsgm.ui.components.organism.SystemControlAction
import com.example.vfsgm.ui.components.organism.SystemControlPanel
import com.example.vfsgm.viewmodel.MainViewModel

@Composable
fun AppScreen(viewModel: MainViewModel = viewModel()) {
    val sessionState by viewModel.sessionState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()
    val appConfigState by viewModel.appConfigState.collectAsState()

    Column(
        modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        SystemControlPanel(
            appConfig = appConfigState,
            dataState = dataState,
            onAction = { systemControlAction ->
                when (systemControlAction) {
                    is SystemControlAction.AppConfigChangeRequest -> viewModel.updateAppConfig(
                        systemControlAction.appConfig
                    )

                    SystemControlAction.StartReLogin -> viewModel.startPeriodicReLogin()
                    SystemControlAction.StopReLogin -> viewModel.stopPeriodicReLogin()
                }
            })

        Text(
            text = appConfigState.toString()
        )

        AnimatedContent(
            targetState = sessionState, label = "AuthStateTransition"
        ) { accessToken ->
            when (accessToken == null) {
                true -> AuthControlPanel(
                    onLoginClick = viewModel::login
                )

                false -> AppControlPanel(
                    dataState = dataState, onAction = { action ->
                        when (action) {
                            AppControlAction.LoadApplicants -> viewModel.loadApplicants()
                            AppControlAction.AddApplicants -> viewModel.addApplicant()
                            AppControlAction.LoadCalendar -> viewModel.loadCalender()
                            AppControlAction.Logout -> viewModel.logout()
                            AppControlAction.StartCheckIsSlotAvailable -> viewModel.startCheckIsSlotAvailable()
                            AppControlAction.StopCheckIsSlotAvailable -> viewModel.stopCheckIsSlotAvailable()
                            AppControlAction.LoadSlot -> viewModel.loadTimeSlot()
                            AppControlAction.StopLoadSlot -> viewModel.stopLoadTimeSlot()
                        }
                    })
            }
        }
    }
}