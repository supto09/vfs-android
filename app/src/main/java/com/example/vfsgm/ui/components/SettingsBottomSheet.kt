package com.example.vfsgm.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vfsgm.data.dto.AppConfig
import com.example.vfsgm.data.dto.Entry
import com.example.vfsgm.ui.components.atomics.MyOutLinedButton
import com.example.vfsgm.ui.components.atomics.MySolidButton
import com.example.vfsgm.ui.components.atomics.OutlinedButtonVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    appConfig: AppConfig,
    entry: Entry,
    onAppConfigChangeRequested: (AppConfig) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showSheet by remember { mutableStateOf(false) }
    var spinCount by remember { mutableIntStateOf(0) }
    val gearRotation by animateFloatAsState(
        targetValue = spinCount * 360f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "settingsGearSpin"
    )

    var deviceIndex by remember { mutableIntStateOf(appConfig.deviceIndex) }
    var entryIndex by remember { mutableIntStateOf(appConfig.entryIndex) }

    Box {
        IconButton(
            onClick = {
                spinCount += 1
                deviceIndex = appConfig.deviceIndex
                entryIndex = appConfig.entryIndex
                showSheet = true
            }
        ) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = "Setting",
                tint = Color(0xFFE85D75),
                modifier = Modifier.graphicsLayer {
                    rotationZ = gearRotation
                }
            )
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x14FFFFFF)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Current Entry",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x1FFFFFFF), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Field",
                                    modifier = Modifier.weight(0.95f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Value",
                                    modifier = Modifier.weight(1.25f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            TableRow(
                                key = "Country",
                                value = entry.countryCode
                            )
                            TableRow(
                                key = "Mission",
                                value = entry.missionCode
                            )
                            TableRow(
                                key = "Center",
                                value = entry.vacCode
                            )
                            TableRow(
                                key = "Visa",
                                value = entry.visaCategoryCode
                            )
                            TableRow(
                                key = "Applicants",
                                value = entry.applicants.size.toString()
                            )

                            if (entry.applicants.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Applicant List",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x1FFFFFFF), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Name",
                                        modifier = Modifier.weight(1.4f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Passport",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                entry.applicants.forEach { applicant ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${applicant.firstName} ${applicant.lastName}".trim(),
                                            modifier = Modifier.weight(1.4f),
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = applicant.passportNumber.ifBlank { "-" },
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = if (deviceIndex == 0) "" else deviceIndex.toString(),
                            onValueChange = { deviceIndex = it.toIntOrNull() ?: 0 },
                            label = { Text("Device Index") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE85D75),
                                focusedLabelColor = Color(0xFFE85D75),
                                cursorColor = Color(0xFFE85D75)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = if (entryIndex == 0) "" else entryIndex.toString(),
                            onValueChange = { entryIndex = it.toIntOrNull() ?: 0 },
                            label = { Text("Entry Index") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE85D75),
                                focusedLabelColor = Color(0xFFE85D75),
                                cursorColor = Color(0xFFE85D75)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

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
                                println("Applying device index: $deviceIndex, entry index: $entryIndex")
                                onAppConfigChangeRequested(
                                    AppConfig(
                                        deviceIndex = deviceIndex,
                                        entryIndex = entryIndex
                                    )
                                )
                            },
                        ) {
                            Text("Apply")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }


}

@Composable
private fun TableRow(
    key: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = key,
            modifier = Modifier.weight(0.95f),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f)
        )
        Text(
            text = value.ifBlank { "-" },
            modifier = Modifier.weight(1.25f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
