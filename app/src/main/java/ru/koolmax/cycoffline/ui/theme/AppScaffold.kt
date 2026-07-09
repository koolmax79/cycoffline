package ru.koolmax.cycoffline.ui.theme

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import ru.koolmax.cycoffline.presentation.ui.calendar.CalendarScreen
import ru.koolmax.cycoffline.presentation.ui.device.SyncDeviceScreen
import ru.koolmax.cycoffline.presentation.ui.deviceList.ScanBLEScreen
import ru.koolmax.cycoffline.presentation.ui.fitList.FitListScreen
import ru.koolmax.cycoffline.presentation.ui.navigation.BottomBar
import ru.koolmax.cycoffline.presentation.ui.navigation.Route
import ru.koolmax.cycoffline.presentation.ui.navigation.TopBar
import ru.koolmax.cycoffline.presentation.ui.settings.SettingsScreen
import ru.koolmax.cycoffline.presentation.ui.statistics.StatisticsScreen
import ru.koolmax.cycoffline.presentation.ui.workout.WorkoutScreen

//@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppScaffold(startDestination: String, navController: NavHostController, viewModel: AppScaffoldViewModel = hiltViewModel()) {
    val bottomBarState = remember { (mutableStateOf(true)) }
    val topBarState = rememberSaveable { (mutableStateOf(true)) }
    val bottomButton = remember { mutableStateOf(listOf<Route>() ) }
    val topTitle = rememberSaveable { mutableStateOf("") }
    val countNotDisplayedFit by remember { viewModel.fitSesNotDisplayedCount }.collectAsState()

    navController.addOnDestinationChangedListener { controller,
                                                    destination,
                                                    arguments ->
        when (destination.route) {
            Route.SyncDevice.route -> {
                topBarState.value = false
                bottomBarState.value = true
                bottomButton.value = listOf(
                    Route.SyncDevice,
                    Route.FitList,
                    Route.Calendar,
                    Route.Statistics,
                    Route.Settings
                )
            }
            Route.FitList.route -> {
                topBarState.value = false
                bottomBarState.value = true
                bottomButton.value = listOf(
                    Route.SyncDevice,
                    Route.FitList,
                    Route.Calendar,
                    Route.Statistics,
                    Route.Settings
                )
            }
            Route.Calendar.route -> {
                topBarState.value = false
                bottomBarState.value = true
                bottomButton.value = listOf(
                    Route.SyncDevice,
                    Route.FitList,
                    Route.Calendar,
                    Route.Statistics,
                    Route.Settings
                )
            }
            Route.Statistics.route -> {
                topBarState.value = false
                bottomBarState.value = true
                bottomButton.value = listOf(
                    Route.SyncDevice,
                    Route.FitList,
                    Route.Calendar,
                    Route.Statistics,
                    Route.Settings
                )
            }
            Route.Settings.route -> {
                topBarState.value = false
                bottomBarState.value = true
                bottomButton.value = listOf(
                    Route.SyncDevice,
                    Route.FitList,
                    Route.Calendar,
                    Route.Statistics,
                    Route.Settings
                )
            }
            Route.Workout.route -> {
                topBarState.value = false
                bottomBarState.value = false
            }
            Route.ScanBLE.route -> {
                topBarState.value = true
                topTitle.value = "Поиск устройств"
                bottomBarState.value = false
            }
        }
    }

    Scaffold(bottomBar = {
        BottomBar(navController, bottomBarState, bottomButton, countNotDisplayedFit)
        },
        topBar = {
            if(topBarState.value)
                TopBar(navController, topTitle.value)
        }
    ) { padding ->
        NavHost(navController = navController, graph = getNavGraph(startDestination, navController, padding), modifier = Modifier.padding(padding))
    }
}

fun getNavGraph(
    startDestination: String,
    controller: NavController,
    paddingValues: PaddingValues,
): NavGraph {
    return controller.createGraph(startDestination) {
        composable(Route.ScanBLE.route) {
            ScanBLEScreen(controller, modifier = Modifier.padding(paddingValues))
        }
        composable(Route.FitList.route) {
            FitListScreen(controller)
        }
        composable(Route.Calendar.route) {
            CalendarScreen(controller, modifier = Modifier.padding(paddingValues))
        }
        composable(Route.Statistics.route) {
            StatisticsScreen(controller, modifier = Modifier.padding(paddingValues))
        }
        composable(Route.Settings.route) {
            SettingsScreen(controller, modifier = Modifier.padding(paddingValues))
        }
        composable(route = Route.Workout.route,
            arguments = listOf(navArgument("fit") {
                type = NavType.StringType
                nullable = false
            })) {
            WorkoutScreen(it.arguments?.getString("fit")!!)
        }
        composable(route = Route.SyncDevice.route,
            arguments = listOf(navArgument("device") {
                type = NavType.StringType
                nullable = false
            })) {
            SyncDeviceScreen(controller)
        }
    }
}