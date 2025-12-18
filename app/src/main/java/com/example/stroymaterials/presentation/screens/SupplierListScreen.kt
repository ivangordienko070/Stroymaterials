// presentation/screens/SupplierListScreen.kt
package com.example.stroymaterials.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stroymaterials.data.database.entities.SupplierEntity
import com.example.stroymaterials.presentation.components.SearchBar
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.AuthViewModel
import com.example.stroymaterials.presentation.viewmodels.SupplierViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierListScreen(
    navController: NavController,
    viewModel: SupplierViewModel,
    authViewModel: AuthViewModel,
    onSupplierClick: (Long) -> Unit,
    onAddSupplier: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "admin"
    val suppliers by viewModel.suppliers.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Обрабатываем поиск
    LaunchedEffect(searchQuery.value) {
        viewModel.searchSuppliers(searchQuery.value)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Поставщики") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = false }
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Главное меню")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onAddSupplier) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить поставщика")
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Поиск
            SearchBar(
                query = searchQuery.value,
                onQueryChange = { searchQuery.value = it },
                placeholder = "Поиск поставщиков...",
                modifier = Modifier.padding(16.dp)
            )

            // Список поставщиков
            if (suppliers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Нет поставщиков",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Нет поставщиков")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suppliers) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            onClick = { onSupplierClick(supplier.id) },
                            onEditClick = if (isAdmin) {
                                { navController.navigate(Screen.SupplierEdit.createRoute(supplier.id)) }
                            } else null,
                            onDeleteClick = if (isAdmin) {
                                {
                                    scope.launch {
                                        try {
                                            viewModel.deleteSupplier(supplier)
                                            snackbarHostState.showSnackbar("Поставщик удален")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Ошибка удаления: ${e.message}")
                                        }
                                    }
                                }
                            } else null,
                            isAdmin = isAdmin
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierCard(
    supplier: SupplierEntity,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    isAdmin: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!supplier.isActive) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Text("Неактивен", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (isAdmin && onEditClick != null) {
                        IconButton(
                            onClick = { onEditClick() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Редактировать",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (isAdmin && onDeleteClick != null) {
                        IconButton(
                            onClick = { onDeleteClick() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            Text(
                text = supplier.contactPerson,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = supplier.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (supplier.city != null) {
                Text(
                    text = "${supplier.city}, ${supplier.address}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = supplier.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Рейтинг: ${"★".repeat(supplier.rating)}${"☆".repeat(5 - supplier.rating)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Доставка: ${supplier.deliveryTimeDays} дн.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}