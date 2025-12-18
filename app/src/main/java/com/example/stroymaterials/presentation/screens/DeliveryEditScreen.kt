// presentation/screens/DeliveryEditScreen.kt
package com.example.stroymaterials.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stroymaterials.data.database.AppDatabase
import com.example.stroymaterials.data.database.entities.DeliveryEntity
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.data.database.entities.SupplierEntity
import com.example.stroymaterials.data.repositories.MaterialRepository
import com.example.stroymaterials.data.repositories.SupplierRepository
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.DeliveryViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryEditScreen(
    deliveryId: Long,
    viewModel: DeliveryViewModel,
    navController: NavController
) {
    val isEditing = deliveryId > 0
    var delivery by remember { mutableStateOf<DeliveryEntity?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Загружаем поставку для редактирования
    LaunchedEffect(deliveryId) {
        if (isEditing && deliveryId > 0) {
            try {
                delivery = viewModel.getDeliveryById(deliveryId)
            } catch (e: Exception) {
                // Если ошибка загрузки, создаем новую поставку
                val calendar = Calendar.getInstance()
                val expectedDate = calendar.time
                calendar.add(Calendar.DAY_OF_MONTH, 3)
                val deliveryDate = calendar.time
                
                // Получаем первый доступный материал и поставщика
                val database = AppDatabase.getDatabase(context)
                val materialRepo = MaterialRepository(database.materialDao())
                val supplierRepo = SupplierRepository(database.supplierDao())
                
                val materials = materialRepo.getAllMaterials().first()
                val suppliers = supplierRepo.getAllSuppliers().first()
                
                val materialId = materials.firstOrNull()?.id ?: 0L
                val supplierId = suppliers.firstOrNull()?.id ?: 0L
                
                delivery = DeliveryEntity(
                    materialId = materialId,
                    supplierId = supplierId,
                    quantity = 0.0,
                    deliveryDate = deliveryDate,
                    expectedDate = expectedDate,
                    status = "pending",
                    invoiceNumber = "",
                    totalCost = 0.0
                )
            }
        } else {
            val calendar = Calendar.getInstance()
            val expectedDate = calendar.time
            calendar.add(Calendar.DAY_OF_MONTH, 3)
            val deliveryDate = calendar.time
            
            // Получаем первый доступный материал и поставщика
            val database = AppDatabase.getDatabase(context)
            val materialRepo = MaterialRepository(database.materialDao())
            val supplierRepo = SupplierRepository(database.supplierDao())
            
            val materials = materialRepo.getAllMaterials().first()
            val suppliers = supplierRepo.getAllSuppliers().first()
            
            val materialId = materials.firstOrNull()?.id ?: 0L
            val supplierId = suppliers.firstOrNull()?.id ?: 0L
            
            delivery = DeliveryEntity(
                materialId = materialId,
                supplierId = supplierId,
                quantity = 0.0,
                deliveryDate = deliveryDate,
                expectedDate = expectedDate,
                status = "pending",
                invoiceNumber = "",
                totalCost = 0.0
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (isEditing) "Редактировать поставку" else "Новая поставка")
                },
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
                    IconButton(onClick = {
                        scope.launch {
                            delivery?.let {
                                // Валидация данных
                                if (it.invoiceNumber.isBlank()) {
                                    snackbarHostState.showSnackbar(
                                        "Введите номер накладной",
                                        duration = SnackbarDuration.Short
                                    )
                                    return@launch
                                }
                                // Проверяем, что материал и поставщик выбраны
                                if (it.materialId <= 0) {
                                    snackbarHostState.showSnackbar(
                                        "Выберите материал",
                                        duration = SnackbarDuration.Short
                                    )
                                    return@launch
                                }
                                if (it.supplierId <= 0) {
                                    snackbarHostState.showSnackbar(
                                        "Выберите поставщика",
                                        duration = SnackbarDuration.Short
                                    )
                                    return@launch
                                }
                                
                                try {
                                    if (isEditing) {
                                        viewModel.updateDelivery(it.copy(updatedAt = Date()))
                                        snackbarHostState.showSnackbar(
                                            "Поставка обновлена",
                                            duration = SnackbarDuration.Short
                                        )
                                    } else {
                                        val newDelivery = it.copy(
                                            createdAt = Date(),
                                            updatedAt = Date()
                                        )
                                        val id = viewModel.addDelivery(newDelivery)
                                        snackbarHostState.showSnackbar(
                                            "Поставка добавлена",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    // Небольшая задержка перед возвратом, чтобы пользователь увидел сообщение
                                    kotlinx.coroutines.delay(500)
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        "Ошибка сохранения: ${e.message}",
                                        duration = SnackbarDuration.Long
                                    )
                                    e.printStackTrace()
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
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
            delivery?.let {
                DeliveryEditForm(
                    delivery = it,
                    onDeliveryChange = { updatedDelivery ->
                        delivery = updatedDelivery
                    },
                    context = context
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryEditForm(
    delivery: DeliveryEntity,
    onDeliveryChange: (DeliveryEntity) -> Unit,
    context: android.content.Context
) {
    val database = AppDatabase.getDatabase(context)
    val materialRepo = MaterialRepository(database.materialDao())
    val supplierRepo = SupplierRepository(database.supplierDao())
    
    // Загружаем материалы и поставщиков
    var materials by remember { mutableStateOf<List<MaterialEntity>>(emptyList()) }
    var suppliers by remember { mutableStateOf<List<SupplierEntity>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        materialRepo.getAllMaterials().collect { materialsList ->
            materials = materialsList
        }
    }
    
    LaunchedEffect(Unit) {
        supplierRepo.getAllSuppliers().collect { suppliersList ->
            suppliers = suppliersList
        }
    }
    
    // Находим выбранный материал и поставщика
    val selectedMaterial = materials.find { it.id == delivery.materialId }
    val selectedSupplier = suppliers.find { it.id == delivery.supplierId }
    
    var materialExpanded by remember { mutableStateOf(false) }
    var supplierExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Номер накладной
        OutlinedTextField(
            value = delivery.invoiceNumber,
            onValueChange = { onDeliveryChange(delivery.copy(invoiceNumber = it)) },
            label = { Text("Номер накладной") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Выбор материала
        ExposedDropdownMenuBox(
            expanded = materialExpanded,
            onExpandedChange = { materialExpanded = !materialExpanded }
        ) {
            OutlinedTextField(
                value = selectedMaterial?.name ?: "Выберите материал",
                onValueChange = {},
                readOnly = true,
                label = { Text("Материал") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = materialExpanded,
                onDismissRequest = { materialExpanded = false }
            ) {
                if (materials.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Нет материалов в базе") },
                        onClick = { materialExpanded = false }
                    )
                } else {
                    materials.forEach { material ->
                        DropdownMenuItem(
                            text = { Text("${material.name} (ID: ${material.id})") },
                            onClick = {
                                onDeliveryChange(delivery.copy(materialId = material.id))
                                materialExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Выбор поставщика
        ExposedDropdownMenuBox(
            expanded = supplierExpanded,
            onExpandedChange = { supplierExpanded = !supplierExpanded }
        ) {
            OutlinedTextField(
                value = selectedSupplier?.name ?: "Выберите поставщика",
                onValueChange = {},
                readOnly = true,
                label = { Text("Поставщик") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = supplierExpanded,
                onDismissRequest = { supplierExpanded = false }
            ) {
                if (suppliers.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Нет поставщиков в базе") },
                        onClick = { supplierExpanded = false }
                    )
                } else {
                    suppliers.forEach { supplier ->
                        DropdownMenuItem(
                            text = { Text("${supplier.name} (ID: ${supplier.id})") },
                            onClick = {
                                onDeliveryChange(delivery.copy(supplierId = supplier.id))
                                supplierExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Количество
        OutlinedTextField(
            value = delivery.quantity.toString(),
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                onDeliveryChange(delivery.copy(quantity = value))
            },
            label = { Text("Количество") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Общая стоимость
        OutlinedTextField(
            value = delivery.totalCost.toString(),
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                onDeliveryChange(delivery.copy(totalCost = value))
            },
            label = { Text("Общая стоимость (руб.)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Статус
        var statusExpanded by remember { mutableStateOf(false) }
        val statuses = listOf("pending", "in_transit", "delivered", "cancelled")
        val statusLabels = mapOf(
            "pending" to "Ожидается",
            "in_transit" to "В пути",
            "delivered" to "Доставлено",
            "cancelled" to "Отменено"
        )

        ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = !statusExpanded }
        ) {
            OutlinedTextField(
                value = statusLabels[delivery.status] ?: delivery.status,
                onValueChange = {},
                readOnly = true,
                label = { Text("Статус") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(statusLabels[status] ?: status) },
                        onClick = {
                            onDeliveryChange(delivery.copy(status = status))
                            statusExpanded = false
                        }
                    )
                }
            }
        }

        // Примечания
        OutlinedTextField(
            value = delivery.notes ?: "",
            onValueChange = { onDeliveryChange(delivery.copy(notes = it.ifBlank { null })) },
            label = { Text("Примечания (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
}

