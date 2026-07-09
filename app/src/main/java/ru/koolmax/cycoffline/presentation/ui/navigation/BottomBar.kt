package ru.koolmax.cycoffline.presentation.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

//@ExperimentalAnimationApi
@Composable
fun BottomBar(navController: NavController, barState: MutableState<Boolean>, items: MutableState<List<Route>>, countNotDisplayedFit: Int) {
    AnimatedVisibility(
        visible = barState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        content = {
            BottomAppBar() {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.value.forEach { item ->
                    val title = stringResource( item.title)
                    NavigationBarItem(
                        icon = {
                            BadgedBox(badge = {
                                if( item.route == Route.FitList.route && countNotDisplayedFit > 0) {
                                    Badge { Text(text = countNotDisplayedFit.toString()) }
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = title
                                )
                            }
                        },
                        //label = { Text(text = title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    )
}