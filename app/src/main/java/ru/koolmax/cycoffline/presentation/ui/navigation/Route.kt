package ru.koolmax.cycoffline.presentation.ui.navigation

import ru.koolmax.cycoffline.R

sealed class Route(val title: Int, val icon: Int, val route: String, badge: Int = 0) {
    object SyncDevice : Route(R.string.sync_device, R.drawable.sync_arrow_down, "sync_device/{device}")
    object ScanBLE : Route(R.string.empty, R.drawable.bluetooth_searching_24, "scan_ble")
    object FitList : Route(R.string.fit_list, R.drawable.list_24, "fit_list")
    object Calendar : Route(R.string.calendar, R.drawable.calendar_month_24, "calendar")
    object Statistics : Route(R.string.statistics, R.drawable.auto_graph_24, "statistics")
    object Settings : Route(R.string.settings, R.drawable.manage_accounts_24, "settings")
    object Workout : Route(R.string.workout, R.drawable.manage_accounts_24, "workout_chart/{fit}")
}