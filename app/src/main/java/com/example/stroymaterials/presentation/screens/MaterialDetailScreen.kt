// presentation/screens/MaterialDetailScreen.kt
package com.example.stroymaterials.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.AuthViewModel
import com.example.stroymaterials.presentation.viewmodels.MaterialViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(
    materialId: Long,
    navController: NavController,
    viewModel: MaterialViewModel,
    authViewModel: AuthViewModel,
    onEditClick: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "admin"
    // Состояние для материала
    var material by remember { mutableStateOf<MaterialEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Загружаем материал при изменении materialId
    LaunchedEffect(materialId) {
        if (materialId <= 0) {
            isLoading = false
            material = null
            return@LaunchedEffect
        }
        isLoading = true
        material = null
        try {
            // Получаем материал из ViewModel
            val loadedMaterial = viewModel.getMaterialById(materialId)
            material = loadedMaterial
            isLoading = false
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Ошибка загрузки материала: ${e.message ?: "Неизвестная ошибка"}")
            }
            isLoading = false
            material = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Детали материала") },
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
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                material?.let {
                                    try {
                                        viewModel.deleteMaterial(it)
                                        navController.popBackStack()
                                        snackbarHostState.showSnackbar("Материал удален")
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
            } else if (material == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Материал не найден")
                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Вернуться назад")
                        }
                    }
                }
            } else {
                MaterialDetailContent(material = material!!)
            }
        }
    }
}

@Composable
fun MaterialDetailContent(material: MaterialEntity) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Карточка с основной информацией
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Название
                DetailItem(
                    label = "Название",
                    value = material.name,
                    isHighlighted = true
                )

                // Тип и единица измерения
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = "Тип",
                            value = material.type
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = "Единица измерения",
                            value = material.unit
                        )
                    }
                }

                // Количество и цена
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = "Количество",
                            value = String.format("%.2f", material.quantity)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = "Цена за единицу",
                            value = "${String.format("%.2f", material.price)} руб."
                        )
                    }
                }

                // Общая стоимость
                DetailItem(
                    label = "Общая стоимость",
                    value = "${String.format("%.2f", material.quantity * material.price)} руб.",
                    fontWeight = FontWeight.Bold,
                    isHighlighted = true
                )
            }
        }

        // Карточка с уровнями запаса
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Уровни запаса",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailItem(
                            label = "Минимальный запас",
                            value = String.format("%.2f", material.minStockLevel)
                        )
                    }

                    material.maxStockLevel?.let { maxLevel ->
                        Box(modifier = Modifier.weight(1f)) {
                            DetailItem(
                                label = "Максимальный запас",
                                value = String.format("%.2f", maxLevel)
                            )
                        }
                    }
                }

                // Статус запаса
                val stockStatus = when {
                    material.quantity <= material.minStockLevel -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                DetailItem(
                                    label = "Статус запаса",
                                    value = "НИЗКИЙ ЗАПАС!",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    material.maxStockLevel != null && material.quantity >= material.maxStockLevel -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                DetailItem(
                                    label = "Статус запаса",
                                    value = "ПРЕВЫШЕН МАКСИМУМ",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    else -> DetailItem(
                        label = "Статус запаса",
                        value = "В НОРМЕ"
                    )
                }

                stockStatus
            }
        }

        // Дополнительная информация
        material.lastDeliveryDate?.let { date ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    DetailItem(
                        label = "Последняя поставка",
                        value = dateFormat.format(date)
                    )
                }
            }
        }

        material.warehouseLocation?.let { location ->
            if (location.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        DetailItem(
                            label = "Расположение на складе",
                            value = location
                        )
                    }
                }
            }
        }

        material.description?.let { description ->
            if (description.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Описание",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Статус активности
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Статус",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Badge(
                        containerColor = if (material.isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        contentColor = if (material.isActive)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onError
                    ) {
                        Text(if (material.isActive) "Активен" else "Не активен")
                    }
                }
            }
        }

        // Информация о записи
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Информация о записи",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ID поставщика",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = material.supplierId.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ID материала",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = material.id.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Создан",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(material.createdAt),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column {
                        Text(
                            text = "Обновлен",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(material.updatedAt),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    fontWeight: FontWeight = FontWeight.Normal,
    isHighlighted: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = fontWeight,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}