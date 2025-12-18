// presentation/screens/DeliveryListScreen.kt
package com.example.stroymaterials.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stroymaterials.data.database.entities.DeliveryEntity
import com.example.stroymaterials.presentation.components.SearchBar
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.AuthViewModel
import com.example.stroymaterials.presentation.viewmodels.DeliveryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryListScreen(
    navController: NavController,
    viewModel: DeliveryViewModel,
    authViewModel: AuthViewModel,
    onDeliveryClick: (Long) -> Unit,
    onAddDelivery: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "admin"
    val deliveries by viewModel.deliveries.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Обновляем список при появлении экрана
    LaunchedEffect(Unit) {
        viewModel.loadDeliveries()
    }
    
    // Обрабатываем поиск
    LaunchedEffect(searchQuery.value) {
        viewModel.searchDeliveries(searchQuery.value)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Поставки") },
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
                FloatingActionButton(onClick = onAddDelivery) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить поставку")
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
                placeholder = "Поиск по накладным...",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (deliveries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = "Нет поставок",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет поставок",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Добавьте первую поставку",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deliveries) { delivery ->
                        DeliveryCard(
                            delivery = delivery,
                            onClick = { onDeliveryClick(delivery.id) },
                            onEditClick = if (isAdmin) {
                                { navController.navigate(Screen.DeliveryEdit.createRoute(delivery.id)) }
                            } else null,
                            onDeleteClick = if (isAdmin) {
                                {
                                    scope.launch {
                                        try {
                                            viewModel.deleteDelivery(delivery)
                                            snackbarHostState.showSnackbar("Поставка удалена")
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
fun DeliveryCard(
    delivery: DeliveryEntity,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    isAdmin: Boolean = false
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Накладная №${delivery.invoiceNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(status = delivery.status)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                text = "Дата: ${dateFormat.format(delivery.expectedDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Количество: ${String.format("%.2f", delivery.quantity)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Материал ID: ${delivery.materialId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${String.format("%.2f", delivery.totalCost)} руб.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, displayText) = when (status.lowercase()) {
        "delivered" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Доставлено"
        )
        "pending" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Ожидается"
        )
        "in_transit" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "В пути"
        )
        "cancelled" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Отменено"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            status
        )
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = displayText,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
