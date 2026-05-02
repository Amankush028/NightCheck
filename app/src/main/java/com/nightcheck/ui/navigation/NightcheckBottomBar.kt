package com.aco.nightcheck.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nightcheck.ui.navigation.Screen
import com.nightcheck.ui.theme.LocalNightcheckColors

private data class NavItemConfig(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Declared at file level — constant, never reallocated
private val navItems = listOf(
    NavItemConfig(Screen.Home,     Icons.Filled.Home,                    Icons.Outlined.Home),
    NavItemConfig(Screen.Tasks,    Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    NavItemConfig(Screen.Notes,    Icons.Filled.Description,             Icons.Outlined.Description),
    NavItemConfig(Screen.Settings, Icons.Filled.Person,                  Icons.Outlined.Person),
)

@Composable
fun NightcheckBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute    = backStackEntry?.destination?.route
    val scheme          = MaterialTheme.colorScheme
    val nc              = LocalNightcheckColors.current

    // Remember the glow brush — only recreated when primary color changes
    val glowBrush = remember(scheme.primary) {
        Brush.radialGradient(
            colors = listOf(scheme.primary.copy(alpha = 0.3f), Color.Transparent)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(0.5.dp, scheme.outline, RoundedCornerShape(32.dp)),
            shape          = RoundedCornerShape(32.dp),
            color          = scheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier              = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.screen.route

                    // Stable nav lambda — captured by route string, not by navController ref
                    val navigate = remember(item.screen.route) {
                        {
                            navController.navigate(item.screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    }

                    NavItem(
                        item      = item,
                        selected  = selected,
                        glowBrush = glowBrush,
                        tintColor = if (selected) scheme.primary else nc.textMuted,
                        onClick   = if (selected) ({}) else navigate
                    )
                }
            }
        }
    }
}

/**
 * Extracted stable subcomposable — skips recomposition for items that haven't
 * changed selection state, since all its parameters are stable primitives.
 */
@Composable
private fun NavItem(
    item: NavItemConfig,
    selected: Boolean,
    glowBrush: Brush,
    tintColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier         = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(glowBrush)
            )
        }
        Icon(
            imageVector        = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = null,
            tint               = tintColor,
            modifier           = Modifier.size(24.dp)
        )
    }
}