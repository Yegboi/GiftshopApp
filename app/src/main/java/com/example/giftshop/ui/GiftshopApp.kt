package com.example.giftshop.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.giftshop.ui.screens.CartScreen
import com.example.giftshop.ui.screens.ProductDetailScreen
import com.example.giftshop.ui.screens.ProductListScreen

/** Navigation routes. Kept in one place so the argument names cannot drift. */
object Routes {
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PRODUCT_ID_ARG = "productId"
    const val PRODUCT_PATTERN = "product/{$PRODUCT_ID_ARG}"

    fun product(id: String) = "product/$id"
}

/**
 * Root of the UI. The cart view model is created here — outside any nav
 * destination — so it is scoped to the activity and shared by every screen.
 */
@Composable
fun GiftshopApp() {
    val navController = rememberNavController()
    val cart: CartViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.CATALOG) {

        composable(Routes.CATALOG) {
            ProductListScreen(
                cart = cart,
                onProductClick = { navController.navigate(Routes.product(it)) },
                onCartClick = { navController.navigate(Routes.CART) },
            )
        }

        composable(
            route = Routes.PRODUCT_PATTERN,
            arguments = listOf(navArgument(Routes.PRODUCT_ID_ARG) { type = NavType.StringType }),
        ) { entry ->
            ProductDetailScreen(
                productId = entry.arguments?.getString(Routes.PRODUCT_ID_ARG).orEmpty(),
                cart = cart,
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) },
            )
        }

        composable(Routes.CART) {
            CartScreen(
                cart = cart,
                onBack = { navController.popBackStack() },
                onContinueShopping = {
                    navController.popBackStack(Routes.CATALOG, inclusive = false)
                },
            )
        }
    }
}
