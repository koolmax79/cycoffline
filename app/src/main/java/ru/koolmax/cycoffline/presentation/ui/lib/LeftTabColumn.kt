package ru.koolmax.cycoffline.presentation.ui.lib

import android.R.attr.layout
import android.R.attr.x
import android.R.attr.y
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import kotlin.collections.forEach

@Composable
fun LeftTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    indicator: @Composable () -> Unit = {
        Box(modifier = Modifier.leftTabIndicatorOffset(selectedTabIndex = selectedTabIndex, matchContentSize = true)
            .width(3.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primary))
    },
    tabs: @Composable () -> Unit,
) {
    Surface( modifier = modifier .selectableGroup(), color = containerColor, contentColor = contentColor) {
        SubcomposeLayout(modifier = modifier.fillMaxHeight()) { constraints ->
            val tabMeasurables = subcompose("tabs") {
                Column(modifier = Modifier.width(IntrinsicSize.Min), verticalArrangement = Arrangement.SpaceAround) {
                    tabs()
                }
            }
            val tabPlaceables = tabMeasurables.map { it.measure(constraints) }
            val maxSize = tabPlaceables.fold(IntSize.Zero) { currentMax, placeable ->
                IntSize(
                    width = maxOf(currentMax.width, placeable.width),
                    height = maxOf(currentMax.height, placeable.height)
                )
            }

            layout(width = maxSize.width, height = maxSize.height) {
                tabPlaceables.forEach { placeable ->
                    //Log.i("cycoffline1", placeable.)
                    placeable.placeRelative(0, 0)
                }
            }
        }
    }
}

//private data class TabPosition(val left: Dp, val width: Dp, val contentWidth: Dp)

/*private class LeftTabIndicatorModifier(private val tabPositions: androidx.compose.runtime.State<List<TabPosition>>, private val selectedTabIndex: Int, private val matchContentSize: Boolean): Modifier {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LeftTabIndicatorModifier) {
            return false
        }
        return tabPositions == other.tabPositions && selectedTabIndex == other.selectedTabIndex && matchContentSize == other.matchContentSize
    }
    override fun hashCode(): Int {
        var result = tabPositions.hashCode()
        result = 31 * result + selectedTabIndex
        result = 31 * result + matchContentSize.hashCode()
        return result
    }

    override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R {
        TODO("Not yet implemented")
    }

    override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R {
        TODO("Not yet implemented")
    }

    override fun any(predicate: (Modifier.Element) -> Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun all(predicate: (Modifier.Element) -> Boolean): Boolean {
        TODO("Not yet implemented")
    }
}*/

/*@OptIn(ExperimentalMaterial3Api::class)
private class LeftTabIndicatorScope : TabIndicatorScope {
    private var tabPositionsState = mutableStateOf<List<TabPosition>>(emptyList())
    fun setTabPositions(positions: List<TabPosition>) {
        tabPositionsState.value = positions
    }
    override fun Modifier.tabIndicatorLayout(measure: androidx.compose.ui.layout.MeasureScope.(androidx.compose.ui.layout.Measurable, Constraints, List<TabPosition>) -> androidx.compose.ui.layout.MeasureResult, ): Modifier {
        return this.layout { measurable, constraints ->
            measure( measurable, constraints, tabPositionsState.value)
        }
    }
    override fun Modifier.tabIndicatorOffset( selectedTabIndex: Int, matchContentSize: Boolean, ): Modifier {
        return this.then(LeftTabIndicatorModifier(tabPositions = tabPositionsState, selectedTabIndex = selectedTabIndex, matchContentSize = matchContentSize) )
    }
}*/

@Composable fun Modifier.leftTabIndicatorOffset(selectedTabIndex: Int, matchContentSize: Boolean = false): Modifier = composed {
    this
}

//@OptIn(ExperimentalMaterial3Api::class)
//private fun TabIndicatorScope.leftTabIndicatorOffset(selectedTabIndex: Int, matchContentSize: Boolean, ): Modifier {
//    return tabIndicatorLayout { measurable, constraints, tabPositions ->
//        if (tabPositions.isEmpty()) {
//            return@tabIndicatorLayout layout(0, 0) {}
//        }
//        val index = selectedTabIndex.coerceIn(0, tabPositions.lastIndex)
//        val position = tabPositions[index]
//        val indicatorHeight = if(matchContentSize) {
 //           position.contentWidth
 //       } else {
 //           position.width
 //       }
 //       val placeable = measurable.measure(constraints.copy(minHeight = indicatorHeight.roundToPx(), maxHeight = indicatorHeight.roundToPx()))
 //       layout(width = constraints.maxWidth, height = constraints.maxHeight) {
//            placeable.placeRelative(x = constraints.maxWidth - placeable.width, y = position.left.roundToPx())
//        }
//    }
//}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LeftTabRow2(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    indicator: @Composable TabIndicatorScope.() -> Unit = {
        Box(modifier = Modifier.leftTabIndicatorOffset(selectedTabIndex = selectedTabIndex, matchContentSize = true).width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
    },
    divider: @Composable () -> Unit = {
        VerticalDivider()
    },
    tabs: @Composable ColumnScope.() -> Unit
) {
    val scope = remember {
        LeftTabIndicatorScope()
    }
    Surface(modifier = modifier.selectableGroup(),
        color = containerColor, contentColor = contentColor) {
        SubcomposeLayout(modifier = Modifier.fillMaxHeight()) {
            constraints ->
            val tabMeasurables = subcompose("Tabs") {
                Column(modifier = Modifier.width(IntrinsicSize.Min), content = tabs)
            }
            val tabs = tabMeasurables.map { it.measure(constraints.copy( minWidth = 0, minHeight = 0, )) }
            val width = tabs.maxOfOrNull { it.width } ?: 0
            val height = tabs.sumOf { it.height } /* * TabPosition. * * left -> top * width -> height */
            val positions = buildList {
                var top = 0
                tabs.forEach {
                    tab -> add(TabPosition(left = top.toDp(), width = tab.height.toDp(), contentWidth = tab.height.toDp()))
                    top += tab.height
                }
            }
            scope.setTabPositions(positions)
            val indicatorPlaceables = subcompose("Indicator") {
                scope.indicator()
            }.map { it.measure( Constraints.fixed( width = width, height = height)) }
            val dividerPlaceables = subcompose("Divider") {
                divider()
            }.map { it.measure( Constraints( minWidth = 0, maxWidth = width, minHeight = 0, maxHeight = height)) }
            layout(width, height) {
                var y = 0
                tabs.forEach {
                    tab -> tab.placeRelative( x = 0, y = y)
                    y += tab.height
                }
                dividerPlaceables.forEach { it.placeRelative( x = width - it.width, y = 0, ) }
                indicatorPlaceables.forEach { it.placeRelative( x = 0, y = 0, ) }
            }
        }
    }
}

private class LeftTabIndicatorModifier(
    private val tabPositions: androidx.compose.runtime.State<List<TabPosition>>,
    private val selectedTabIndex: Int,
    private val matchContentSize: Boolean): Modifier {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if (other !is LeftTabIndicatorModifier) {
            return false
        }
        return tabPositions == other.tabPositions && selectedTabIndex == other.selectedTabIndex && matchContentSize == other.matchContentSize
    }
    override fun hashCode(): Int {
        var result = tabPositions.hashCode()
        result = 31 * result + selectedTabIndex
        result = 31 * result + matchContentSize.hashCode()
        return result
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private class LeftTabIndicatorScope: TabIndicatorScope {
    private var tabPositionsState = mutableStateOf<List<TabPosition>>(emptyList())
    fun setTabPositions(positions: List<TabPosition>) {
        tabPositionsState.value = positions
    }
    override fun Modifier.tabIndicatorLayout(measure: androidx.compose.ui.layout.MeasureScope.(androidx.compose.ui.layout.Measurable, Constraints, List<TabPosition>) -> androidx.compose.ui.layout.MeasureResult): Modifier {
        return this.layout { measurable, constraints -> measure( measurable, constraints, tabPositionsState.value, ) } }
    override fun Modifier.tabIndicatorOffset(selectedTabIndex: Int, matchContentSize: Boolean, ): Modifier {
        return this.then(LeftTabIndicatorModifier(tabPositions = tabPositionsState, selectedTabIndex = selectedTabIndex, matchContentSize = matchContentSize))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun TabIndicatorScope.leftTabIndicatorOffset(selectedTabIndex: Int, matchContentSize: Boolean): Modifier {
    return tabIndicatorLayout {
        measurable, constraints, tabPositions -> if(tabPositions.isEmpty()) {
            return@tabIndicatorLayout layout(0, 0) {}
        }
        val index = selectedTabIndex.coerceIn(0, tabPositions.lastIndex)
        val position = tabPositions[index]
        val indicatorHeight = if(matchContentSize) {
            position.contentWidth
        } else {
            position.width
        }
        val placeable = measurable.measure(constraints.copy(minHeight = indicatorHeight.roundToPx(), maxHeight = indicatorHeight.roundToPx()))
        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            placeable.placeRelative(x = constraints.maxWidth - placeable.width, y = position.left.roundToPx())
        }
    }
}*/