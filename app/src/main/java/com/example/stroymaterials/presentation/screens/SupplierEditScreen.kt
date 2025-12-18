// presentation/screens/SupplierEditScreen.kt
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
import com.example.stroymaterials.data.database.entities.SupplierEntity
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.SupplierViewModel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierEditScreen(
    supplierId: Long,
    viewModel: SupplierViewModel,
    navController: NavController
) {
    val isEditing = supplierId > 0
    var supplier by remember { mutableStateOf<SupplierEntity?>(null) }
    val scope = rememberCoroutineScope()

    // Загружаем поставщика для редактирования
    LaunchedEffect(supplierId) {
        if (isEditing && supplierId > 0) {
            try {
                supplier = viewModel.getSupplierById(supplierId)
            } catch (e: Exception) {
                supplier = SupplierEntity(
                    name = "",
                    contactPerson = "",
                    phone = "",
                    address = ""
                )
            }
        } else {
            supplier = SupplierEntity(
                name = "",
                contactPerson = "",
                phone = "",
                address = ""
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (isEditing) "Редактировать поставщика" else "Новый поставщик")
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
                            supplier?.let {
                                if (isEditing) {
                                    viewModel.updateSupplier(it.copy(updatedAt = Date()))
                                } else {
                                    viewModel.addSupplier(it)
                                }
                                navController.popBackStack()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            supplier?.let {
                SupplierEditForm(
                    supplier = it,
                    onSupplierChange = { updatedSupplier ->
                        supplier = updatedSupplier
                    }
                )
            }
        }
    }
}

@Composable
fun SupplierEditForm(
    supplier: SupplierEntity,
    onSupplierChange: (SupplierEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Название
        OutlinedTextField(
            value = supplier.name,
            onValueChange = { onSupplierChange(supplier.copy(name = it)) },
            label = { Text("Название компании") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Контактное лицо
        OutlinedTextField(
            value = supplier.contactPerson,
            onValueChange = { onSupplierChange(supplier.copy(contactPerson = it)) },
            label = { Text("Контактное лицо") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Телефон
        OutlinedTextField(
            value = supplier.phone,
            onValueChange = { onSupplierChange(supplier.copy(phone = it)) },
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Email
        OutlinedTextField(
            value = supplier.email ?: "",
            onValueChange = { onSupplierChange(supplier.copy(email = it.ifBlank { null })) },
            label = { Text("Email (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Адрес
        OutlinedTextField(
            value = supplier.address,
            onValueChange = { onSupplierChange(supplier.copy(address = it)) },
            label = { Text("Адрес") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Город
        OutlinedTextField(
            value = supplier.city ?: "",
            onValueChange = { onSupplierChange(supplier.copy(city = it.ifBlank { null })) },
            label = { Text("Город (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Рейтинг
        OutlinedTextField(
            value = supplier.rating.toString(),
            onValueChange = {
                val value = it.toIntOrNull()?.coerceIn(1, 5) ?: 5
                onSupplierChange(supplier.copy(rating = value))
            },
            label = { Text("Рейтинг (1-5)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Срок доставки
        OutlinedTextField(
            value = supplier.deliveryTimeDays.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 7
                onSupplierChange(supplier.copy(deliveryTimeDays = value))
            },
            label = { Text("Средний срок доставки (дней)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Условия оплаты
        OutlinedTextField(
            value = supplier.paymentTerms ?: "",
            onValueChange = { onSupplierChange(supplier.copy(paymentTerms = it.ifBlank { null })) },
            label = { Text("Условия оплаты (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Примечания
        OutlinedTextField(
            value = supplier.notes ?: "",
            onValueChange = { onSupplierChange(supplier.copy(notes = it.ifBlank { null })) },
            label = { Text("Примечания (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
}

