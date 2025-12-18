// presentation/screens/SupplierDetailScreen.kt
package com.example.stroymaterials.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stroymaterials.data.database.entities.SupplierEntity
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.AuthViewModel
import com.example.stroymaterials.presentation.viewmodels.SupplierViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(
    supplierId: Long,
    navController: NavController,
    viewModel: SupplierViewModel,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "admin"
    var supplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(supplierId) {
        if (supplierId > 0) {
            try {
                supplier = viewModel.getSupplierById(supplierId)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Детали поставщика") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = false }
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Главное меню")
                    }
                    if (isAdmin) {
                        IconButton(onClick = {
                            supplier?.let {
                                navController.navigate(Screen.SupplierEdit.createRoute(it.id))
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                supplier?.let {
                                    try {
                                        viewModel.deleteSupplier(it)
                                        navController.popBackStack()
                                        snackbarHostState.showSnackbar("Поставщик удален")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Ошибка удаления: ${e.message}")
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (supplier == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Поставщик не найден")
                }
            } else {
                SupplierDetailContent(supplier = supplier!!)
            }
        }
    }
}

@Composable
fun SupplierDetailContent(supplier: SupplierEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Название и статус
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = supplier.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Статус
            Badge(
                containerColor = if (supplier.isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                contentColor = if (supplier.isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onError
            ) {
                Text(if (supplier.isActive) "Активен" else "Не активен")
            }
        }

        Divider()

        // Контактное лицо
        DetailItemWithIcon(
            icon = Icons.Default.Person,
            label = "Контактное лицо",
            value = supplier.contactPerson
        )

        // Телефон
        DetailItemWithIcon(
            icon = Icons.Default.Phone,
            label = "Телефон",
            value = supplier.phone
        )

        // Email
        supplier.email?.let { email ->
            DetailItemWithIcon(
                icon = Icons.Default.Email,
                label = "Email",
                value = email
            )
        }

        // Адрес
        DetailItemWithIcon(
            icon = Icons.Default.LocationOn,
            label = "Адрес",
            value = supplier.address
        )

        // Город
        supplier.city?.let { city ->
            DetailItemWithIcon(
                icon = Icons.Default.LocationCity,
                label = "Город",
                value = city
            )
        }

        Divider()

        // Рейтинг
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Рейтинг поставщика",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Звезды рейтинга
            RatingStars(rating = supplier.rating)
        }

        // Срок доставки
        DetailItem(
            label = "Средний срок доставки",
            value = "${supplier.deliveryTimeDays} дней"
        )

        // Условия оплаты
        supplier.paymentTerms?.let { terms ->
            DetailItem(
                label = "Условия оплаты",
                value = terms
            )
        }

        // Примечания
        supplier.notes?.let { notes ->
            DetailItem(
                label = "Примечания",
                value = notes
            )
        }
    }
}

@Composable
fun DetailItemWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RatingStars(rating: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Рейтинг $rating из 5",
                tint = if (i <= rating) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}