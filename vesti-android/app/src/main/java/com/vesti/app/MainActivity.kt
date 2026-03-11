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
import com.vesti.app.data.local.TokenManager
import com.vesti.app.data.network.RetrofitClient
import com.vesti.app.data.repository.AuthRepository
import com.vesti.app.ui.auth.AuthViewModel
import com.vesti.app.ui.auth.LoginScreen
import com.vesti.app.ui.auth.RegisterScreen
import com.vesti.app.ui.theme.VestiColors
import com.vesti.app.ui.theme.VestiTheme
import com.vesti.app.ui.wardrobe.WardrobeScreen
import com.vesti.app.ui.wardrobe.WardrobeViewModel
import com.vesti.app.ui.outfit.OutfitScreen
import com.vesti.app.ui.outfit.OutfitViewModel
import com.vesti.app.ui.marketplace.MarketplaceScreen
import com.vesti.app.ui.marketplace.MarketplaceViewModel
import com.vesti.app.ui.checkout.CheckoutScreen
import com.vesti.app.ui.checkout.CheckoutViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual Dependency Injection for simplicity
        val tokenManager = TokenManager(applicationContext)
        val authApi = RetrofitClient.getAuthApi(tokenManager)
        val authRepository = AuthRepository(authApi, tokenManager)
        // Note: Using a standard ViewModel instantiation here for simplicity. 
        // In a real app, use a ViewModelProvider.Factory or Hilt/Dagger.
        val authViewModel = AuthViewModel(authRepository)
        
        val wardrobeApi = RetrofitClient.getWardrobeApi(tokenManager)
        val wardrobeViewModel = WardrobeViewModel(wardrobeApi)

        val aiApi = RetrofitClient.getAiApi(tokenManager)
        val weatherApi = RetrofitClient.getWeatherApi()
        val outfitViewModel = OutfitViewModel(aiApi, weatherApi)

        val marketplaceApi = RetrofitClient.getMarketplaceApi(tokenManager)
        val marketplaceViewModel = MarketplaceViewModel(marketplaceApi)

        val paymentApi = RetrofitClient.getPaymentApi(tokenManager)
        val checkoutViewModel = CheckoutViewModel(paymentApi)

        setContent {
            VestiTheme {
                val topLevelNavController = rememberNavController()
                
                NavHost(
                    navController = topLevelNavController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToRegister = { topLevelNavController.navigate("register") },
                            onLoginSuccess = { 
                                topLevelNavController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onNavigateBack = { topLevelNavController.popBackStack() },
                            onRegisterSuccess = { 
                                topLevelNavController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main") {
                        MainAppScreen(wardrobeViewModel, outfitViewModel, marketplaceViewModel, checkoutViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(
    wardrobeViewModel: WardrobeViewModel,
    outfitViewModel: OutfitViewModel,
    marketplaceViewModel: MarketplaceViewModel,
    checkoutViewModel: CheckoutViewModel
) {
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
            composable("wardrobe") { WardrobeScreen(viewModel = wardrobeViewModel) }
            composable("outfit") { OutfitScreen(viewModel = outfitViewModel) }
            composable("marketplace") { 
                MarketplaceScreen(
                    viewModel = marketplaceViewModel,
                    onNavigateToCheckout = { itemId, price ->
                        navController.navigate("checkout/$itemId/$price")
                    }
                ) 
            }
            composable("profile") { PlaceholderScreen("Profil") }
            composable(
                route = "checkout/{itemId}/{price}",
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                    navArgument("price") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                val price = backStackEntry.arguments?.getFloat("price")?.toDouble() ?: 0.0
                CheckoutScreen(
                    itemId = itemId,
                    price = price,
                    viewModel = checkoutViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
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
