package com.vesti.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Person
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

        val prefs = applicationContext.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("is_first_time", true)
        val startDest = if (isFirstTime) "onboarding" else "login"

        setContent {
            VestiTheme {
                val topLevelNavController = rememberNavController()
                
                NavHost(
                    navController = topLevelNavController,
                    startDestination = startDest
                ) {
                    composable("onboarding") {
                        com.vesti.app.ui.onboarding.OnboardingScreen(
                            onNavigateToRegister = {
                                prefs.edit().putBoolean("is_first_time", false).apply()
                                topLevelNavController.navigate("register") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                prefs.edit().putBoolean("is_first_time", false).apply()
                                topLevelNavController.navigate("login") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }
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
        bottomBar = { VestiBottomNavigationBar(navController = navController) },
        containerColor = VestiColors.Background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { 
                com.vesti.app.ui.home.HomeScreen(
                    onNavigateToOutfit = { navController.navigate("outfit") },
                    onNavigateToWardrobe = { navController.navigate("wardrobe") },
                    onNavigateToMarket = { navController.navigate("marketplace") }
                ) 
            }
            composable("wardrobe") { WardrobeScreen(viewModel = wardrobeViewModel) }
            composable("outfit") { OutfitScreen(viewModel = outfitViewModel) }
            composable("marketplace") { 
                com.vesti.app.ui.marketplace.MarketplaceScreen(
                    viewModel = marketplaceViewModel,
                    onNavigateToCheckout = { itemId, _ ->
                        navController.navigate("product/$itemId")
                    },
                    onNavigateToMessages = { navController.navigate("messages") }
                ) 
            }
            composable("profile") { com.vesti.app.ui.profile.ProfileScreen() }
            composable("messages") { 
                com.vesti.app.ui.messages.MessagesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChat = { uid -> navController.navigate("chat/$uid") }
                ) 
            }
            composable("product/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                com.vesti.app.ui.marketplace.ProductDetailScreen(
                    productId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { pId, price -> navController.navigate("checkout/$pId/$price") },
                    onNavigateToChat = { sId -> navController.navigate("chat/$sId") }
                )
            }
            composable("chat/{userId}", arguments = listOf(navArgument("userId") { type = NavType.StringType })) { backStackEntry ->
                val uId = backStackEntry.arguments?.getString("userId") ?: ""
                com.vesti.app.ui.messages.ChatDetailScreen(
                    userId = uId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
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
    Box(modifier = Modifier.fillMaxSize().background(VestiColors.Background), contentAlignment = Alignment.Center) {
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
    
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Checkroom,
        Icons.Default.AutoAwesome,
        Icons.Default.Storefront,
        Icons.Default.Person
    )

    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.White,
        contentColor = VestiColors.TextMain,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = icons[index], contentDescription = stringResource(id = labels[index])) },
                label = { Text(stringResource(id = labels[index]), fontSize = 10.sp) },
                selected = currentRoute == screen,
                onClick = {
                    navController.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VestiColors.Primary,
                    selectedTextColor = VestiColors.Primary,
                    indicatorColor = VestiColors.LightPurple,
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                )
            )
        }
    }
}
