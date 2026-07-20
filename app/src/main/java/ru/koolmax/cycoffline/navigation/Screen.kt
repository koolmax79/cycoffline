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

    object FitInfo: Screen("")
    object FitCharts: Screen("")
    object FitStatistics: Screen("")
}

sealed class BarItem(val screen: Screen, val title: Int, val icon: Int, badge: Int = 0) {
    object Device : BarItem(Screen.Devices, R.string.sync_device, R.drawable.sync_arrow_down)
    object Fit : BarItem(Screen.Fit,R.string.fit_list, R.drawable.list_24)
    object Calendar : BarItem(Screen.Calendar, R.string.calendar, R.drawable.calendar_month_24)
    object Statistics : BarItem(Screen.Statistics, R.string.statistics, R.drawable.auto_graph_24)
    object Settings : BarItem(Screen.Settings, R.string.settings, R.drawable.manage_accounts_24)

    object FitInfo : BarItem(Screen.FitInfo, R.string.info,R.drawable.page_info_24px)
    object FitCharts : BarItem(Screen.FitCharts,R.string.charts, R.drawable.line_axis_24px)
    object FitStatistics : BarItem(Screen.FitStatistics, R.string.statistics, R.drawable.line_axis_24px)

    companion object {
        val mainBottoms = listOf(Device, Fit, Calendar, Statistics, Settings)
        val fitTabs = listOf(FitInfo, FitCharts, FitStatistics)
    }
}