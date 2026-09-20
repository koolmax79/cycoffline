package ru.koolmax.cycoffline.navigation

import ru.koolmax.cycoffline.R

sealed class Screen(val route: String) {
    object Devices: Screen("devices")
        object SyncDeviceScreen: Screen("syncDeviceScreen")
        object ScanBLE: Screen("scanBLE")

    object Fit: Screen("fit")
        object FitList: Screen("fitList")
        object Workout: Screen("workout/{fit}")
    object Calendar: Screen("calendar")
    object Statistics: Screen("statistics")
    object Settings: Screen("settings")

    object FitInfo: Screen("fitInfo")
    object FitCharts: Screen("fitCharts")
    object FitStatistics: Screen("fitStatistics")
}

sealed class BarItem(val screen: Screen, val title: Int, val icon: Int, badge: Int = 0) {
    object Device : BarItem(Screen.Devices, R.string.sync_device, R.drawable.sync_bike_computer)
    object Fit : BarItem(Screen.Fit,R.string.fit_list, R.drawable.training_list)
    object Calendar : BarItem(Screen.Calendar, R.string.calendar, R.drawable.calendar)
    object Statistics : BarItem(Screen.Statistics, R.string.statistics, R.drawable.statistics)
    object Settings : BarItem(Screen.Settings, R.string.settings, R.drawable.user_profile)

    object FitInfo : BarItem(Screen.FitInfo, R.string.info,R.drawable.info)
    object FitCharts : BarItem(Screen.FitCharts,R.string.charts, R.drawable.graphs)
    object FitStatistics : BarItem(Screen.FitStatistics, R.string.statistics, R.drawable.statistics)

    companion object {
        val empty = listOf<BarItem>()
        val mainBottoms = listOf(Device, Fit, Calendar, Statistics, Settings)
        val fitTabs = listOf(FitInfo, FitCharts, FitStatistics)
    }
}