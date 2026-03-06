package com.vesti.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vesti.app.ui.theme.VestiColors
import com.vesti.app.ui.theme.VestiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VestiTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { VestiBottomNavigationBar(navController = navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { PlaceholderScreen("Ana Sayfa (Dashboard)") }
                        composable("wardrobe") { PlaceholderScreen("Gardırop") }
                        composable("outfit") { PlaceholderScreen("Kombin (AI)") }
                        composable("marketplace") { PlaceholderScreen("Market / 2. El") }
                        composable("profile") { PlaceholderScreen("Profil") }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = VestiColors.TextMain)
    }
}

@Composable
fun VestiBottomNavigationBar(navController: NavHostController) {
    val items = listOf("home", "wardrobe", "outfit", "marketplace", "profile")
    val labels = listOf(
        R.string.nav_home,
        R.string.nav_wardrobe,
        R.string.nav_outfit,
        R.string.nav_market,
        R.string.nav_profile
    )
    
    // For now we use standard icons, ideally replace with custom drawables later
    val icons = listOf(
        android.R.drawable.ic_menu_compass,
        android.R.drawable.ic_menu_gallery,
        android.R.drawable.ic_menu_camera,
        android.R.drawable.ic_menu_shop,
        android.R.drawable.ic_menu_manage
    )

    NavigationBar(
        containerColor = VestiColors.DarkIndigo,
        contentColor = VestiColors.Background
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = icons[index]), contentDescription = stringResource(id = labels[index])) },
                label = { Text(stringResource(id = labels[index])) },
                selected = currentRoute == screen,
                onClick = {
                    navController.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VestiColors.Accent,
                    selectedTextColor = VestiColors.Accent,
                    indicatorColor = VestiColors.DarkIndigo,
                    unselectedIconColor = VestiColors.Background,
                    unselectedTextColor = VestiColors.Background
                )
            )
        }
    }
}
