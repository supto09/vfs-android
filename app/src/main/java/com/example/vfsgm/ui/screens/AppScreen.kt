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
import com.example.vfsgm.ui.components.AppControlPanel
import com.example.vfsgm.ui.components.AuthControlPanel
import com.example.vfsgm.ui.components.CloudflareModalWrapper
import com.example.vfsgm.ui.components.TurnstileWebviewModal
import com.example.vfsgm.viewmodel.MainViewModel

@Composable
fun AppScreen(viewModel: MainViewModel = viewModel()) {
    val sessionState by viewModel.sessionState.collectAsState()

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CloudflareModalWrapper()

        TurnstileWebviewModal()

        AnimatedContent(
            targetState = sessionState.accessToken,
            label = "AuthStateTransition"
        ) { accessToken ->
            when (accessToken.isNullOrEmpty()) {
                true -> AuthControlPanel(
                    onLoginClick = viewModel::login
                )

                false -> AppControlPanel(
                    onLoadApplicant = viewModel::loadApplicants,
                    onAddApplicant = viewModel::addApplicant,
                    onGenderLoad = viewModel::getGender,
                    onLogout = viewModel::logout
                )
            }
        }
    }
}