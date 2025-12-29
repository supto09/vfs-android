package com.example.vfsgm.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.vfsgm.data.constants.SITE_KEY
import com.example.vfsgm.data.store.TurnstileStore
import com.example.vfsgm.ui.components.atomics.MySolidButton
import kotlin.let


@Composable
fun TurnstileWebviewModal() {
    val showDialog = remember { mutableStateOf(false) }

    Box {
        MySolidButton(
            onClick = { showDialog.value = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("TT")
        }

        if (showDialog.value) {
            Dialog(onDismissRequest = { showDialog.value = false }) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                ) {
                    TurnstileTokenWebview(
                        siteKey = SITE_KEY,
                        onToken = { token ->
                            println("Turnstile Token: $token")
                            token?.let { it: String ->
                                TurnstileStore.setToken(token = it)
                                println(it)
                                showDialog.value = false
                            }
                        },
                        onClose = {
                            println("On Complete received")
                            // Optional callback once cf_clearance is acquired
                            showDialog.value = false
                        }
                    )
                }
            }
        }
    }
}
