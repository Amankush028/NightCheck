package com.aco.nightcheck.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nightcheck.ui.navigation.Screen

private data class NavItemConfig(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navItems = listOf(
    NavItemConfig(Screen.Home,     "Home",     Icons.Filled.Home,        Icons.Outlined.Home),
    NavItemConfig(Screen.Tasks,    "Tasks",    Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    NavItemConfig(Screen.Notes,    "Notes",    Icons.Filled.Notes,       Icons.Outlined.Notes),
    NavItemConfig(Screen.Settings, "Profile",  Icons.Filled.Settings,    Icons.Outlined.Settings), // Using Profile from HTML
)

@Composable
fun NightcheckBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(72.dp)
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.screen.route

            // Customizing colors to remove the standard background pill
            val colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                indicatorColor = Color.Transparent // Disables standard background pill
            )

            NavigationBarItem(
                selected = selected,
                colors = colors,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                // Build our custom label logic with the active dot underneath
                label = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                        )
                        if (selected) {
                            Spacer(Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            // Empty spacer to prevent layout jumping when selecting
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            )
        }
    }
}