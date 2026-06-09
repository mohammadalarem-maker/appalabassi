package com.supermarket.app.ui.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.supermarket.app.ui.home.DashboardScreen
import com.supermarket.app.ui.home.HomeViewModel
import com.supermarket.app.ui.home.MainScreen
import com.supermarket.app.ui.inventory.InventoryScreen
import com.supermarket.app.ui.inventory.AddEditProductScreen
import com.supermarket.app.ui.login.LoginScreen
import com.supermarket.app.ui.login.LoginViewModel
import com.supermarket.app.ui.sales.SalesScreen
import com.supermarket.app.ui.sales.NewSaleScreen
import com.supermarket.app.ui.reports.ReportsScreen
import com.supermarket.app.ui.users.UsersScreen
import com.supermarket.app.ui.customers.CustomersScreen
import com.supermarket.app.ui.purchases.PurchasesScreen
import com.supermarket.app.ui.expenses.ExpensesScreen
import com.supermarket.app.ui.settings.SettingsScreen
import com.supermarket.app.ui.notifications.NotificationsScreen

sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Home          : Screen("home")
    object Inventory     : Screen("inventory")
    object AddProduct    : Screen("add_product?id={id}") {
        fun createRoute(id: String? = null) = if (id != null) "add_product?id=$id" else "add_product"
    }
    object Sales         : Screen("sales")
    object NewSale       : Screen("new_sale")
    object Reports       : Screen("reports")
    object Users         : Screen("users")
    object Customers     : Screen("customers")
    object Purchases     : Screen("purchases")
    object Expenses      : Screen("expenses")
    object Settings      : Screen("settings")
    object Notifications : Screen("notifications")
    object Expiring      : Screen("expiring")
}

// All routes that show inside the drawer shell
private val drawerRoutes = setOf(
    "home","inventory","sales","new_sale","reports","users",
    "customers","purchases","expenses","settings","notifications","expiring","add_product"
)

@Composable
fun AppNavigation(navController: NavHostController) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val startDestination = if (loginViewModel.isLoggedIn()) Screen.Home.route else Screen.Login.route
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route?.substringBefore("?") ?: "home"

    NavHost(navController = navController, startDestination = startDestination) {

        // Login - no drawer
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { _ ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = loginViewModel
            )
        }

        // All screens inside MainScreen (drawer shell)
        composable(Screen.Home.route) {
            MainScreen(
                currentRoute = "home",
                onNavigate = { navController.navigate(it) },
                onLogout = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            ) { DashboardScreen(onNavigate = { navController.navigate(it) }) }
        }

        composable(Screen.Inventory.route) {
            MainScreen("inventory", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) {
                InventoryScreen(
                    onAddProduct = { navController.navigate(Screen.AddProduct.createRoute()) },
                    onEditProduct = { navController.navigate(Screen.AddProduct.createRoute(it)) }
                )
            }
        }

        composable(Screen.AddProduct.route,
            arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
        ) { back ->
            val productId = back.arguments?.getString("id")
            MainScreen("add_product", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) {
                AddEditProductScreen(
                    productId = productId,
                    onBack    = { navController.popBackStack() },
                    onSaved   = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Sales.route) {
            MainScreen("sales", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) {
                SalesScreen(onNewSale = { navController.navigate(Screen.NewSale.route) })
            }
        }

        composable(Screen.NewSale.route) {
            MainScreen("new_sale", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) {
                NewSaleScreen(
                    onBack        = { navController.popBackStack() },
                    onSaleComplete= { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Reports.route) {
            MainScreen("reports", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) { ReportsScreen() }
        }

        composable(Screen.Users.route) {
            MainScreen("users", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) { UsersScreen() }
        }

        composable(Screen.Customers.route) {
            MainScreen("customers", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) { CustomersScreen() }
        }

        composable(Screen.Purchases.route) {
            MainScreen("purchases", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) { PurchasesScreen() }
        }

        composable(Screen.Expenses.route) {
            MainScreen("expenses", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) { ExpensesScreen() }
        }

        composable(Screen.Settings.route) {
            MainScreen("settings", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) {
                SettingsScreen(onLogout = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                })
            }
        }

        composable(Screen.Notifications.route) {
            MainScreen("notifications", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) { NotificationsScreen() }
        }

        composable(Screen.Expiring.route) {
            MainScreen("expiring", { navController.navigate(it) }, {
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            }) {
                InventoryScreen(
                    onAddProduct  = { navController.navigate(Screen.AddProduct.createRoute()) },
                    onEditProduct = { navController.navigate(Screen.AddProduct.createRoute(it)) },
                    showExpiring  = true
                )
            }
        }
    }
}
