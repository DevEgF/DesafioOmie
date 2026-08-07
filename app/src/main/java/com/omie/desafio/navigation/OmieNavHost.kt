package com.omie.desafio.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.feature.devtools.presentation.DeveloperModeScreenRoot
import com.omie.desafio.feature.products.presentation.form.ProductFormScreenRoot
import com.omie.desafio.feature.products.presentation.list.ProductListScreenRoot
import com.omie.desafio.feature.sales.presentation.detail.SaleDetailScreenRoot
import com.omie.desafio.feature.sales.presentation.home.HomeScreenRoot
import com.omie.desafio.feature.sales.presentation.newsale.NewSaleScreenRoot

@Composable
fun OmieNavHost(navController: NavHostController, analyticsTracker: AnalyticsTracker) {
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            analyticsTracker.logScreenView(destination.route.orEmpty())
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(navController = navController, startDestination = Route.Home) {

        composable<Route.Home> {
            HomeScreenRoot(
                onNavigateToNewSale = { navController.navigate(Route.NewSale) },
                onNavigateToProducts = { navController.navigate(Route.ProductList) },
                onNavigateToSaleDetail = { saleId -> navController.navigate(Route.SaleDetail(saleId)) },
                onNavigateToDeveloperMode = { navController.navigate(Route.DeveloperMode) },
            )
        }

        composable<Route.SaleDetail> {
            SaleDetailScreenRoot(onBack = { navController.popBackStack() })
        }

        composable<Route.DeveloperMode> {
            DeveloperModeScreenRoot(onBack = { navController.popBackStack() })
        }

        composable<Route.NewSale> {
            NewSaleScreenRoot(
                onSaleSaved = { navController.popBackStack() },
                onCancelled = { navController.popBackStack() },
            )
        }

        composable<Route.ProductList> {
            ProductListScreenRoot(
                onNavigateToAddProduct = { navController.navigate(Route.ProductForm()) },
                onNavigateToEditProduct = { productId ->
                    navController.navigate(
                        Route.ProductForm(
                            productId
                        )
                    )
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.ProductForm> {
            ProductFormScreenRoot(
                onProductSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
