package com.nightcheck.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nightcheck.ui.navigation.NightcheckBottomBar
import com.nightcheck.ui.navigation.NightcheckNavGraph
import com.nightcheck.ui.navigation.Screen
import com.nightcheck.ui.theme.NightcheckTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.nightcheck.util.PreferencesManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme by preferencesManager.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = systemDark)

            val onThemeToggle: () -> Unit = remember(isDarkTheme) {
                {
                    lifecycleScope.launch {
                        preferencesManager.setDarkTheme(!isDarkTheme)
                    }
                }
            }

            NightcheckTheme(
                darkTheme     = isDarkTheme,
                onThemeToggle = onThemeToggle
            ) {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute   = backStackEntry?.destination?.route

                val bottomNavRoutes = remember { Screen.bottomNavItems.map { it.route }.toHashSet() }
                val showBottomBar   = currentRoute in bottomNavRoutes

                // Use a Box instead of Scaffold so the floating nav bar has NO
                // built-in background Surface — it floats freely over content.
                Box(modifier = Modifier.fillMaxSize()) {
                    NightcheckNavGraph(
                        navController = navController,
                        modifier      = Modifier.fillMaxSize()
                    )

                    if (showBottomBar) {
                        NightcheckBottomBar(
                            navController = navController,
                            modifier      = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}