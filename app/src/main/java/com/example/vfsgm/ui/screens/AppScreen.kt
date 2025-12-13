package com.example.vfsgm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vfsgm.ui.components.CloudflareModalWrapper
import com.example.vfsgm.ui.components.TurnstileWebviewModal
import com.example.vfsgm.viewmodel.MainViewModel

@Composable
fun AppScreen(viewModel: MainViewModel = viewModel()) {

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CloudflareModalWrapper()

        TurnstileWebviewModal()


        Button(
            onClick = viewModel::login,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}