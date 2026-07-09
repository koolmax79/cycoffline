package ru.koolmax.cycoffline.presentation.ui.device

import android.R.attr.direction
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.Log
import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.paint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.utils.Utils
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.DEVICE_STATUS
import ru.koolmax.cycoffline.data.DeviceFile
import ru.koolmax.cycoffline.data.DeviceStatus
import ru.koolmax.cycoffline.data.db.DeviceInfo
import ru.koolmax.cycoffline.presentation.MeasureUtil
import ru.koolmax.cycoffline.presentation.pairToString
import ru.koolmax.cycoffline.presentation.ui.ColoredIcon
import ru.koolmax.cycoffline.presentation.ui.deviceList.DeviceBLECard
import ru.koolmax.cycoffline.presentation.ui.deviceList.DeviceListViewModel
import ru.koolmax.cycoffline.presentation.ui.navigation.Route
import ru.koolmax.cycoffline.presentation.ui.settings.SettingsViewModel
import ru.koolmax.cycoffline.ui.theme.CycofflineTheme
import ru.koolmax.cycoffline.ui.theme.LightCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalIconSize
import ru.koolmax.cycoffline.ui.theme.LocalSpacing
import kotlin.String

@Composable
fun SyncDeviceScreen(navController: NavController, viewModel: DeviceListViewModel = hiltViewModel()) {
    val deviceList by remember { viewModel.deviceList }.collectAsState()
    val deviceFileList = remember { viewModel.deviceFileList }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space100),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Устройства", style = MaterialTheme.typography.headlineMedium )
            IconButton(onClick = {
                navController.navigate(
                    Route.ScanBLE.route
                )
            }, colors = IconButtonDefaults.filledIconButtonColors()) {
                ColoredIcon(
                    modifier = Modifier.size(LocalIconSize.current.size100),
                    drawable = R.drawable.add_24px,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        SavedDevicesList(deviceList,
            onDelete = { viewModel.deleteDevice(it) },
            onLoadFiles = { viewModel.getInfoFromDevice(it) })
        FilesList(deviceFileList, onItemClick = {
            viewModel.saveDeviceFileToLib(it)
        })
    }
}

@Composable
fun SavedDevicesList(deviceList: List<DeviceStatus>, onDelete: (DeviceInfo) -> Unit, onLoadFiles: (DeviceInfo) -> Unit){
        LazyColumn() {
            items(
                items = deviceList,
                key = { result -> "${result.device.address}${result.status.name}" }) {
                SavedDeviceCard(it, onDelete = onDelete, onLoadFiles = onLoadFiles)
            }
        }
}

@Composable
fun FilesList(deviceFileList: SnapshotStateList<DeviceFile>, onItemClick: (DeviceFile) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = deviceFileList, key = { it.name }) {
            DeviceFileCard(it, onItemClick = {
                onItemClick(it)
            })
        }
    }
}

@Composable
fun SavedDeviceCard(device: DeviceStatus, onDelete: (DeviceInfo) -> Unit, onLoadFiles: (DeviceInfo) -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(modifier = Modifier.fillMaxSize(),
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(end = LocalSpacing.current.space100),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(R.string.forget),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(LocalSpacing.current.space100))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(LocalIconSize.current.size100)
                        )
                    }
                }
            }
        },
        content = {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space25)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { onLoadFiles(device.device) },
                    ),
                ) {
                Row(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space100),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column() {
                        Text(
                            text = device.device.address,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = device.device.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    when (device.status) {
                        DEVICE_STATUS.CONNECTED -> {
                            ColoredIcon(modifier = Modifier.size(LocalIconSize.current.size100),
                                drawable = R.drawable.bluetooth_connected_24px,
                                color = LightCustomColorsPalette.iconColorActive)
                        }
                        else -> {
                            ColoredIcon(modifier = Modifier.size(LocalIconSize.current.size100),
                                drawable = R.drawable.bluetooth_24px,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        onDismiss = {
            onDelete(device.device)
    })
}

@Composable
fun DeviceFileCard(deviceFile: DeviceFile, onItemClick: (DeviceFile) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space25)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space25),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(deviceFile.name, style = MaterialTheme.typography.bodyLarge)
                Text(MeasureUtil.getFileSize(deviceFile.size), style = MaterialTheme.typography.bodyMedium)
            }
            when {
                deviceFile.isNotSynchronized -> {
                    Button(
                        onClick = { onItemClick(deviceFile) }) { Text(text = "Загрузить") }
                }

                deviceFile.inQueue -> {
                    Button(
                        enabled = false,
                        onClick = { onItemClick(deviceFile) }) { Text(text = "Загрузить") }
                }

                deviceFile.isSynchronized -> {
                    ColoredIcon(
                        modifier = Modifier.size(LocalIconSize.current.size50),
                        drawable = R.drawable.check_24px,
                        color = LocalCustomColorsPalette.current.iconColorEnabled,
                    )
                }

                deviceFile.isSynchronizing -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(MeasureUtil.getPercent(deviceFile.loadedSize.toFloat() / deviceFile.size.toFloat() * 100)
                                .pairToString(),
                            modifier = Modifier.padding(LocalSpacing.current.space100)
                        )
                        CircularProgressIndicator(
                            progress = { deviceFile.loadedSize.toFloat() / deviceFile.size.toFloat() },
                        )
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun GreetingPreview() {
    //var deviceFile = DeviceFile("20000dsahfsjdhf.fit", DeviceInfo(), 10000, 10000)
    //val deviceStatus = DeviceStatus()
    //DeviceFileCard(deviceFile, deviceStatus, {})
}