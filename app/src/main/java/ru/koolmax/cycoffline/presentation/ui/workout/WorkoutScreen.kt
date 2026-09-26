package ru.koolmax.cycoffline.presentation.ui.workout

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.navigation.BarItem
import ru.koolmax.cycoffline.presentation.ui.ColoredIcon
import ru.koolmax.cycoffline.presentation.ui.lib.LeftTabRow
import ru.koolmax.cycoffline.presentation.ui.lib.leftTabIndicatorOffset
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(fit: String, viewModel: WorkoutViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.getFitSession(fit)
    }

    val tabs = BarItem.fitTabs
    val pagerState = rememberPagerState(pageCount = {
        tabs.size
    })

    when(LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            Column {
                TabsTop(tabs = tabs, pagerState = pagerState)
                TabsContent(tabs = tabs, pagerState = pagerState, fit, viewModel)
            }
        }
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row {
                TabsLeft(tabs = tabs, pagerState = pagerState)
                TabsContent(tabs = tabs, pagerState = pagerState, fit, viewModel)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsTop(tabs: List<BarItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    PrimaryTabRow(selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, tab ->
            LeadingIconTab(
                icon = {
                    Image(
                        painter = painterResource(tab.icon),
                        contentDescription = null
                    )
                },
                text = { },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsLeft(tabs: List<BarItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    LeftTabRow(selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.fillMaxHeight()) {
        tabs.forEachIndexed { index, tab ->
            Box() {
                LeadingIconTab(
                    icon = {
                        Image(
                            painter = painterResource(tab.icon),
                            contentDescription = null
                        )
                    },
                    text = { },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
                if(pagerState.currentPage == index) {
                    Box(modifier = Modifier.
                        width(3.dp)
                        .height(40.dp).align(Alignment.CenterEnd)//IntrinsicSize.Max
                        .background(MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsContent(tabs: List<BarItem>, pagerState: PagerState, fit: String, viewModel: WorkoutViewModel) {
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            when (page) {
                0 -> {
                    WorkoutInfoScreen(viewModel)
                }
                1 -> {
                    WorkoutChartScreen(viewModel)
                }
                2 -> {
                    WorkoutStatisticsScreen(viewModel)
                }
            }
        }
    }
}