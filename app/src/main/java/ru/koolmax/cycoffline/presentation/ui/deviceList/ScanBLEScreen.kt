package ru.koolmax.cycoffline.presentation.ui.deviceList

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import ru.koolmax.cycoffline.data.db.DeviceInfo
import ru.koolmax.cycoffline.data.db.toDeviceInfo
import ru.koolmax.cycoffline.presentation.ui.settings.SettingsViewModel
import ru.koolmax.cycoffline.ui.theme.LocalSpacing

@SuppressLint("ProduceStateDoesNotAssignValue")
@Composable
fun ScanBLEScreen(navController: NavController, modifier: Modifier, viewModel: DeviceListViewModel = hiltViewModel(),
                     settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val scanResultList by remember { viewModel.scanResultList }.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.startScan()
    }
//modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space300)
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space100),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Сканирование", style =  MaterialTheme.typography.headlineMedium)
            CircularProgressIndicator()
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = scanResultList,
                key = { result -> "${result.device.address}-${result.data?.rssi}" }) {
                DeviceBLECard(it, onItemClick = {
                    viewModel.addDevice(it.toDeviceInfo())
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
fun DeviceBLECard(scanResult: BleScanResult, onItemClick: (ServerDevice) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space25)
        .clickable(onClick = { onItemClick(scanResult.device) })) {
        Row(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space25),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column() {
                Text(scanResult.device.address,
                    //fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(scanResult.device.name ?: "", style = MaterialTheme.typography.bodyLarge)
            }
            Text(scanResult.data?.rssi.toString(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true,  uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun GreetingPreview() {
    //Column() {
    //    Row(modifier = Modifier.fillMaxWidth().padding(8.dp),
    //        horizontalArrangement = Arrangement.SpaceBetween) {
    //        Text(text = "Сканирование", style =  MaterialTheme.typography.headlineLarge)
    //        CircularProgressIndicator()
    //    }
        /*LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = scanResultList,
                key = { result -> "${result.device.address} - ${result.data!!.rssi}" }) {
                DeviceBLECard(it, onItemClick = {
                })
            }
        }*/
    //}
}