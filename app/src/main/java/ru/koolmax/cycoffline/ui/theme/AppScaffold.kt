package ru.koolmax.cycoffline.ui.theme

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.createGraph
import androidx.navigation.navArgument
import androidx.navigation.navigation
import ru.koolmax.cycoffline.navigation.BarItem
import ru.koolmax.cycoffline.presentation.ui.calendar.CalendarScreen
import ru.koolmax.cycoffline.presentation.ui.device.SyncDeviceScreen
import ru.koolmax.cycoffline.presentation.ui.deviceList.ScanBLEScreen
import ru.koolmax.cycoffline.presentation.ui.fitList.FitListScreen
import ru.koolmax.cycoffline.navigation.BottomBar
import ru.koolmax.cycoffline.navigation.NavigationState
import ru.koolmax.cycoffline.navigation.Screen
import ru.koolmax.cycoffline.navigation.TopBar
import ru.koolmax.cycoffline.presentation.ui.settings.SettingsScreen
import ru.koolmax.cycoffline.presentation.ui.statistics.StatisticsScreen
import ru.koolmax.cycoffline.presentation.ui.workout.WorkoutScreen

//@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppScaffold(startDestination: String, navigationState: NavigationState, viewModel: AppScaffoldViewModel = hiltViewModel()) {
    val topBarState = rememberSaveable { (mutableStateOf(true)) }
    var bottomButtonList by remember { mutableStateOf(listOf<BarItem>() ) }
    val topTitle = rememberSaveable { mutableStateOf("") }
    val countNotDisplayedFit = remember { viewModel.fitSesNotDisplayedCount }.collectAsState()

    navigationState.navHostController.addOnDestinationChangedListener { controller,
                                                    destination,
                                                    arguments ->
        when (destination.route) {
            Screen.Devices.route -> {
                topBarState.value = false
                bottomButtonList = BarItem.mainBottoms
            }
            Screen.FitList.route -> {
                topBarState.value = false
                bottomButtonList = BarItem.mainBottoms
            }
            Screen.Calendar.route -> {
                topBarState.value = false
                bottomButtonList = BarItem.mainBottoms
            }
            Screen.Statistics.route -> {
                topBarState.value = false
                bottomButtonList = BarItem.mainBottoms
            }
            Screen.Settings.route -> {
                topBarState.value = false
                bottomButtonList = BarItem.mainBottoms
            }
/*
            Route.Workout.route -> {
                topBarState.value = false
                bottomBarState.value = false
            }
            Route.ScanBLE.route -> {
                topBarState.value = true
                topTitle.value = "Поиск устройств"
                bottomBarState.value = false
            }
*/
        }
    }

    val navBackStackEntry by navigationState.navHostController.currentBackStackEntryAsState()
    Scaffold(bottomBar = {
        if(bottomButtonList.isNotEmpty())
            BottomBar(navigationState, navBackStackEntry, bottomButtonList, countNotDisplayedFit)
        },
        topBar = {
            //if(navController.graph.hierarchy)
            //TopBar(navigationState.navHostController, topTitle.value)
        }
    ) { padding ->
        NavHost(
            navController = navigationState.navHostController,
            graph = getNavGraph(startDestination, navigationState.navHostController, padding),
            modifier = Modifier.padding(padding)
        )
    }
}

fun getNavGraph(
    startDestination: String,
    controller: NavController,
    paddingValues: PaddingValues,
): NavGraph {
    return controller.createGraph(startDestination) {
        navigation(route = Screen.Devices.route, startDestination = Screen.SyncDeviceScreen.route) {
            composable(Screen.SyncDeviceScreen.route) {
                SyncDeviceScreen(controller)
            }
            composable(Screen.ScanBLE.route) {
                ScanBLEScreen(controller)
            }
        }
        navigation(route = Screen.Fit.route, startDestination = Screen.FitList.route) {
            composable(Screen.FitList.route) {
                FitListScreen(controller)
            }
            composable(route = Screen.Workout.route,
                arguments = listOf(navArgument("fit") {
                    type = NavType.StringType
                    nullable = false
                })) {
                WorkoutScreen(it.arguments?.getString("fit")!!)
            }
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(controller)
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(controller)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(controller)
        }
    }
}