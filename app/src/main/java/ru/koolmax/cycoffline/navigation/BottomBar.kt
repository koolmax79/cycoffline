package ru.koolmax.cycoffline.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import ru.koolmax.cycoffline.R
import java.nio.file.WatchEvent

@Composable
fun BottomBar(navigationState: NavigationState, navBackStackEntry: NavBackStackEntry?, items: List<BarItem>, countNotDisplayedFit: State<Int>) {
    if(items.isEmpty()) return
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        content = {
            BottomAppBar() {
                items.forEach { item ->
                    val title = stringResource( item.title)
                    NavigationBarItem(
                        icon = {
                            BadgedBox(badge = {
                                if(Screen.Fit == item.screen && countNotDisplayedFit.value > 0) {
                                    Badge { Text(text = countNotDisplayedFit.value.toString()) }
                                }
                            }) {
                                Image(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = title
                                )
                            }
                        },
                        //label = { Text(text = title) },
                        selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == item.screen.route } ?: false,
                        onClick = {
                            navigationState.navigationTo(item.screen.route)
                        }
                    )
                }
            }
        }
    )
}