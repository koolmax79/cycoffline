package ru.koolmax.cycoffline

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import ru.koolmax.cycoffline.presentation.PermissionUtil
import ru.koolmax.cycoffline.navigation.Screen
import ru.koolmax.cycoffline.navigation.rememberNavigationState
import ru.koolmax.cycoffline.ui.theme.AppScaffold
import ru.koolmax.cycoffline.ui.theme.CycofflineTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private fun permissionsList(): Array<String>
    {
        val list = arrayListOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_SCAN)
            list.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            list.add(Manifest.permission.BLUETOOTH_ADMIN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        return list.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtil.checkAndRequest(this, permissionsList())
        enableEdgeToEdge()
        setContent {
            CycofflineTheme {
                Greeting()
            }
        }
    }
}

//@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Greeting() {
    val navigationState = rememberNavigationState()
    AppScaffold(startDestination = Screen.Fit.route, navigationState = navigationState)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CycofflineTheme {
        Greeting()
    }
}