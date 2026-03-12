package com.example.vfsgm.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vfsgm.ui.components.organism.AppControlAction
import com.example.vfsgm.ui.components.organism.AppControlPanel
import com.example.vfsgm.ui.components.organism.AuthControlPanel
import com.example.vfsgm.ui.components.organism.SystemControlAction
import com.example.vfsgm.ui.components.organism.SystemControlPanel
import com.example.vfsgm.ui.theme.DashboardBlue
import com.example.vfsgm.ui.theme.DashboardCyan
import com.example.vfsgm.ui.theme.DashboardIndigo
import com.example.vfsgm.ui.theme.DashboardNavy
import com.example.vfsgm.ui.theme.DashboardSky
import com.example.vfsgm.ui.theme.DashboardTeal
import com.example.vfsgm.viewmodel.MainViewModel

@Composable
fun AppScreen(viewModel: MainViewModel = viewModel()) {
    val sessionState by viewModel.sessionState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()
    val appConfigState by viewModel.appConfigState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DashboardBlue,
                        DashboardNavy,
                        Color(0xFF030712)
                    )
                )
            )
    ) {
        GradientOrb(
            modifier = Modifier.align(Alignment.TopStart),
            size = 260.dp,
            offsetX = (-28).dp,
            offsetY = (-50).dp,
            colors = listOf(DashboardSky.copy(alpha = 0.20f), Color.Transparent)
        )
        GradientOrb(
            modifier = Modifier.align(Alignment.TopEnd),
            size = 220.dp,
            offsetX = 60.dp,
            offsetY = 20.dp,
            colors = listOf(DashboardIndigo.copy(alpha = 0.30f), Color.Transparent)
        )
        GradientOrb(
            modifier = Modifier.align(Alignment.BottomCenter),
            size = 320.dp,
            offsetX = 0.dp,
            offsetY = 150.dp,
            colors = listOf(DashboardTeal.copy(alpha = 0.16f), Color.Transparent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                }
            )

            Text(
                text = appConfigState.toString(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyLarge
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DashboardCyan.copy(alpha = 0.35f),
                                DashboardIndigo.copy(alpha = 0.40f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(top = 1.dp)
            )
            AnimatedContent(
                targetState = sessionState,
                label = "AuthStateTransition"
            ) { accessToken ->
                when (accessToken == null) {
                    true -> AuthControlPanel(
                        onLoginClick = viewModel::login
                    )

                    false -> AppControlPanel(
                        dataState = dataState,
                        onAction = { action ->
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
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientOrb(
    modifier: Modifier = Modifier,
    size: Dp,
    offsetX: Dp,
    offsetY: Dp,
    colors: List<Color>
) {
    Box(
        modifier = modifier
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .blur(28.dp)
            .background(
                brush = Brush.radialGradient(colors = colors),
                shape = CircleShape
            )
    )
}
