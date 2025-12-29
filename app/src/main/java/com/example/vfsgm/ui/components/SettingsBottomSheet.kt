package com.example.vfsgm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.ui.components.atomics.MyOutLinedButton
import com.example.vfsgm.ui.components.atomics.MySolidButton
import com.example.vfsgm.ui.components.atomics.OutlinedButtonVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    appConfig: AppConfig,
    onAppConfigChangeRequested: (AppConfig) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showSheet by remember { mutableStateOf(false) }

    var deviceIndex by remember { mutableIntStateOf(appConfig.deviceIndex) }

    Box {
        IconButton(onClick = { showSheet = true }) {
            Icon(Icons.Rounded.Settings, contentDescription = "Setting")
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                // dragHandle = { BottomSheetDefaults.DragHandle() }, // optional
            ) {
                // ---- Sheet content ----
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "App Configurations",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = if (deviceIndex == 0) "" else deviceIndex.toString(),
                        onValueChange = { deviceIndex = it.toIntOrNull() ?: 0 },
                        label = { Text("Device Index") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MyOutLinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                showSheet = false
                            },
                            variant = OutlinedButtonVariant.Danger
                        ) {
                            Text("Cancel")
                        }

                        MySolidButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                showSheet = false
                                println("Applying device index: $deviceIndex")
                                onAppConfigChangeRequested(
                                    AppConfig(
                                        deviceIndex = deviceIndex
                                    )
                                )
                            },
                        ) {
                            Text("Done")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }


}