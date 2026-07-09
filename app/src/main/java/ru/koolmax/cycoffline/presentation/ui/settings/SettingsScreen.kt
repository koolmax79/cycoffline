package ru.koolmax.cycoffline.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.garmin.fit.Gender
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.HeartZoneInfo
import ru.koolmax.cycoffline.presentation.DateTimeUtil
import ru.koolmax.cycoffline.presentation.ui.ColoredIcon
import ru.koolmax.cycoffline.presentation.ui.lib.DatePickerModal
import ru.koolmax.cycoffline.presentation.ui.lib.HorizontalPicker
import ru.koolmax.cycoffline.presentation.ui.lib.PickerValueFormatter
import ru.koolmax.cycoffline.presentation.ui.navigation.Route
import ru.koolmax.cycoffline.presentation.ui.workout.getTextHeartRate
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalIconSize
import ru.koolmax.cycoffline.ui.theme.LocalSpacing
import java.time.LocalDate
import kotlin.collections.isNotEmpty

@Composable
fun SettingsScreen(navController: NavController, modifier: Modifier, viewModel: SettingsViewModel = hiltViewModel()) {

    val birthday = remember { viewModel.birthday }.collectAsState()
    val gender = remember { viewModel.gender }.collectAsState()
    val autoHR = remember { viewModel.autoHR }.collectAsState()
    val heartZone = remember { viewModel.heartZone }.collectAsState()
    //val autoMaxHR = remember { viewModel.autoMaxHR }.collectAsState()

    Column(modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        DatePickerFieldToModal(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space100), birthday.value, {
            viewModel.setBirthday(it)
        })

        GenderSelector(modifier = Modifier.fillMaxWidth(), gender.value, {
            viewModel.setGender(it)
        })

        Row(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space100),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End) {
            Text(text = "Автоматический режим", style = MaterialTheme.typography.bodyLarge)
            Switch(modifier = Modifier.padding(horizontal = LocalSpacing.current.space100),
                checked = autoHR.value,
                onCheckedChange = {
                    viewModel.setAutoHR(!autoHR.value)
                }
            )
        }

        if(autoHR.value)
            Text(text = heartZone.value.defaultMaxHR.toString(), style = LocalTextStyle.current)
        else {
            val maxHRState = remember { mutableIntStateOf(viewModel.heartZone.value.maxHR) }
            HorizontalPicker(modifier = Modifier.fillMaxWidth(), min = 20, max = 250, maxHRState, heartZone.value.defaultMaxHR)
            LaunchedEffect(maxHRState.intValue) {
                viewModel.setMaxHR(maxHRState.intValue)
            }
        }
        Text(text = "max ЧСС", style = MaterialTheme.typography.labelSmall)

        HeartZones(modifier = Modifier.fillMaxWidth(), heartZone.value)

        //if(autoHR.value)
        //    Text(text = maxHRState.intValue.toString(), style = LocalTextStyle.current)
        //else
        //    HorizontalPicker(modifier = Modifier.fillMaxWidth(), min = 20, max = 250, maxHRState, heartZone.value.defaultMaxHR)
        //Text(text = "ЧСС в покое", style = MaterialTheme.typography.labelSmall)

        //MaxHR(modifier = modifier.fillMaxWidth())
        //Row(
        //    modifier = modifier.fillMaxWidth()
        //        .clickable(onClick = { navController.navigate(Route.DevicesScan.route) }),
        //    horizontalArrangement = Arrangement.SpaceBetween
        //) {
        //    Text(stringResource(R.string.device))
        //    Text(device.name)
        //    Text(device.address)
        //}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerFieldToModal(modifier: Modifier = Modifier, birthday: LocalDate, onClick: (LocalDate) -> Unit) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = birthday.let{ DateTimeUtil.LocalDateToMilliseconds(it) })
    val selectedDate = datePickerState.selectedDateMillis?.let {
        DateTimeUtil.convertMillisecondToDateStr(it)
    } ?: ""
    var showModal by remember { mutableStateOf(false) }

    Row(modifier = modifier.clickable( onClick = { showModal = !showModal } ),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text("День рождения")
        Text(selectedDate)
    }

    if (showModal) {
        DatePickerModal(
            datePickerState = datePickerState,
            onDateSelected = { it?.let { onClick(DateTimeUtil.millisecondsToLocalDate(it)) } },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun GenderSelector(modifier: Modifier = Modifier, gender: Gender, onClick: (Gender) -> Unit) {
    Row(modifier = modifier.selectableGroup().fillMaxWidth().padding(LocalSpacing.current.space25)) {
        Row(modifier = Modifier.weight(1f).selectable(
            selected = (Gender.FEMALE==gender),
            onClick = { onClick(Gender.FEMALE) },
            role = Role.RadioButton),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            ColoredIcon(
                modifier = Modifier.size(LocalIconSize.current.size100),
                drawable = R.drawable.female_24px,
                color = MaterialTheme.colorScheme.primary
            )
            RadioButton(selected = (Gender.FEMALE==gender),
                onClick = null)
        }
        Row(modifier = Modifier.weight(1f).selectable(
            selected = (Gender.MALE==gender),
            onClick = { onClick(Gender.MALE) },
            role = Role.RadioButton),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = (Gender.MALE==gender),
                onClick = null)
            ColoredIcon(
                modifier = Modifier.size(LocalIconSize.current.size100),
                drawable = R.drawable.male_24px,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MaxHR(modifier: Modifier = Modifier) {
    val state = remember { mutableIntStateOf(1) }
    HorizontalPicker(
        modifier = Modifier.fillMaxWidth(),
        min = 10,
        max = 180,
        selectedState = state,
        textStyle = MaterialTheme.typography.titleLarge,
    )
}

@Composable
fun HeartZones(modifier: Modifier = Modifier, heartZone: HeartZone) {
    if(heartZone.list.isNotEmpty()) {
        Column(modifier = modifier) {
            heartZone.list.reversed().forEach {
                Zone(it)
            }
        }
    }
}

@Composable
fun Zone(zone: HeartZoneInfo) {
    val color = LocalCustomColorsPalette.current.getHeartZoneColor(zone.idx)
    Row(modifier = Modifier.padding(2.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text(modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = getTextHeartRate(zone.min, zone.max))
            }
        }
    }
}
