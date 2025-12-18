// presentation/navigation/AppNavigation.kt
package com.example.stroymaterials.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stroymaterials.data.database.AppDatabase
import com.example.stroymaterials.data.repositories.DeliveryRepository
import com.example.stroymaterials.data.repositories.MaterialRepository
import com.example.stroymaterials.data.repositories.SupplierRepository
import com.example.stroymaterials.data.repositories.UserRepository
import com.example.stroymaterials.presentation.screens.*
import com.example.stroymaterials.presentation.viewmodels.AuthViewModel
import com.example.stroymaterials.presentation.viewmodels.AuthViewModelFactory
import com.example.stroymaterials.presentation.viewmodels.DeliveryViewModel
import com.example.stroymaterials.presentation.viewmodels.DeliveryViewModelFactory
import com.example.stroymaterials.presentation.viewmodels.MaterialViewModel
import com.example.stroymaterials.presentation.viewmodels.MaterialViewModelFactory
import com.example.stroymaterials.presentation.viewmodels.SupplierViewModel
import com.example.stroymaterials.presentation.viewmodels.SupplierViewModelFactory

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object MaterialList : Screen("materials")
    object MaterialDetail : Screen("material_detail/{materialId}") {
        fun createRoute(materialId: Long) = "material_detail/$materialId"
    }
    object MaterialEdit : Screen("material_edit/{materialId}") {
        fun createRoute(materialId: Long = -1L) = "material_edit/$materialId"
        fun createRouteForNew() = "material_edit/-1"
    }
    object SupplierList : Screen("suppliers")
    object SupplierDetail : Screen("supplier_detail/{supplierId}") {
        fun createRoute(supplierId: Long) = "supplier_detail/$supplierId"
    }
    object SupplierEdit : Screen("supplier_edit/{supplierId}") {
        fun createRoute(supplierId: Long = -1L) = "supplier_edit/$supplierId"
        fun createRouteForNew() = "supplier_edit/-1"
    }
    object DeliveryList : Screen("deliveries")
    object DeliveryDetail : Screen("delivery_detail/{deliveryId}") {
        fun createRoute(deliveryId: Long) = "delivery_detail/$deliveryId"
    }
    object DeliveryEdit : Screen("delivery_edit/{deliveryId}") {
        fun createRoute(deliveryId: Long = -1L) = "delivery_edit/$deliveryId"
        fun createRouteForNew() = "delivery_edit/-1"
    }
    object Settings : Screen("settings")
    object Analytics : Screen("analytics")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val userRepository = UserRepository(database.userDao())
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Материалы
        composable(Screen.MaterialList.route) {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = MaterialRepository(database.materialDao())
            val viewModel: MaterialViewModel = viewModel(
                factory = MaterialViewModelFactory(repository)
            )
            MaterialListScreen(
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel,
                onMaterialClick = { materialId ->
                    navController.navigate(Screen.MaterialDetail.createRoute(materialId))
                },
                onAddMaterial = {
                    navController.navigate(Screen.MaterialEdit.createRouteForNew())
                }
            )
        }

        composable(
            route = Screen.MaterialDetail.route,
            arguments = listOf(navArgument("materialId") { type = NavType.LongType })
        ) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getLong("materialId") ?: -1L
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = MaterialRepository(database.materialDao())
            val viewModel: MaterialViewModel = viewModel(
                factory = MaterialViewModelFactory(repository)
            )
            MaterialDetailScreen(
                materialId = materialId,
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel,
                onEditClick = {
                    navController.navigate(Screen.MaterialEdit.createRoute(materialId))
                }
            )
        }

        composable(
            route = Screen.MaterialEdit.route,
            arguments = listOf(
                navArgument("materialId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getLong("materialId") ?: -1L
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = MaterialRepository(database.materialDao())
            val viewModel: MaterialViewModel = viewModel(
                factory = MaterialViewModelFactory(repository)
            )
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role != "admin") {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                MaterialEditScreen(
                    materialId = materialId,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }

        // Поставщики
        composable(Screen.SupplierList.route) {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = SupplierRepository(database.supplierDao())
            val viewModel: SupplierViewModel = viewModel(
                factory = SupplierViewModelFactory(repository)
            )
            SupplierListScreen(
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel,
                onSupplierClick = { supplierId ->
                    navController.navigate(Screen.SupplierDetail.createRoute(supplierId))
                },
                onAddSupplier = {
                    navController.navigate(Screen.SupplierEdit.createRouteForNew())
                }
            )
        }

        composable(
            route = Screen.SupplierDetail.route,
            arguments = listOf(navArgument("supplierId") { type = NavType.LongType })
        ) { backStackEntry ->
            val supplierId = backStackEntry.arguments?.getLong("supplierId") ?: -1L
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = SupplierRepository(database.supplierDao())
            val viewModel: SupplierViewModel = viewModel(
                factory = SupplierViewModelFactory(repository)
            )
            SupplierDetailScreen(
                supplierId = supplierId,
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.SupplierEdit.route,
            arguments = listOf(
                navArgument("supplierId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val supplierId = backStackEntry.arguments?.getLong("supplierId") ?: -1L
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = SupplierRepository(database.supplierDao())
            val viewModel: SupplierViewModel = viewModel(
                factory = SupplierViewModelFactory(repository)
            )
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role != "admin") {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                SupplierEditScreen(
                    supplierId = supplierId,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }

        // Поставки
        composable(Screen.DeliveryList.route) {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = DeliveryRepository(database.deliveryDao())
            val viewModel: DeliveryViewModel = viewModel(
                factory = DeliveryViewModelFactory(repository)
            )
            DeliveryListScreen(
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel,
                onDeliveryClick = { deliveryId ->
                    navController.navigate(Screen.DeliveryDetail.createRoute(deliveryId))
                },
                onAddDelivery = {
                    navController.navigate(Screen.DeliveryEdit.createRouteForNew())
                }
            )
        }

        composable(
            route = Screen.DeliveryDetail.route,
            arguments = listOf(navArgument("deliveryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getLong("deliveryId") ?: -1L
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = DeliveryRepository(database.deliveryDao())
            val viewModel: DeliveryViewModel = viewModel(
                factory = DeliveryViewModelFactory(repository)
            )
            DeliveryDetailScreen(
                deliveryId = deliveryId,
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.DeliveryEdit.route,
            arguments = listOf(
                navArgument("deliveryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getLong("deliveryId") ?: -1L
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = DeliveryRepository(database.deliveryDao())
            val viewModel: DeliveryViewModel = viewModel(
                factory = DeliveryViewModelFactory(repository)
            )
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role != "admin") {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                DeliveryEditScreen(
                    deliveryId = deliveryId,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }

        // Аналитика
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }

        // Настройки
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}