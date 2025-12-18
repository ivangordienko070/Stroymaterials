// presentation/screens/MaterialEditScreen.kt
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
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.MaterialViewModel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialEditScreen(
    materialId: Long,
    viewModel: MaterialViewModel,
    navController: NavController
) {
    val isEditing = materialId > 0
    var material by remember { mutableStateOf<MaterialEntity?>(null) }
    val scope = rememberCoroutineScope()

    // Загружаем материал для редактирования
    LaunchedEffect(materialId) {
        if (isEditing && materialId > 0) {
            try {
                material = viewModel.getMaterialById(materialId)
            } catch (e: Exception) {
                // Если материал не найден, создаем новый
                material = MaterialEntity(
                    name = "",
                    type = "",
                    unit = "",
                    quantity = 0.0,
                    price = 0.0,
                    supplierId = 0
                )
            }
        } else {
            // Создание нового материала
            material = MaterialEntity(
                name = "",
                type = "",
                unit = "",
                quantity = 0.0,
                price = 0.0,
                supplierId = 0
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(if (isEditing) "Редактировать материал" else "Новый материал")
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
                            material?.let {
                                if (isEditing) {
                                    viewModel.updateMaterial(it.copy(updatedAt = Date()))
                                } else {
                                    viewModel.addMaterial(it)
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
            material?.let {
                MaterialEditForm(
                    material = it,
                    onMaterialChange = { updatedMaterial ->
                        material = updatedMaterial
                    }
                )
            }
        }
    }
}

@Composable
fun MaterialEditForm(
    material: MaterialEntity,
    onMaterialChange: (MaterialEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Название
        OutlinedTextField(
            value = material.name,
            onValueChange = { onMaterialChange(material.copy(name = it)) },
            label = { Text("Название материала") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Тип
        OutlinedTextField(
            value = material.type,
            onValueChange = { onMaterialChange(material.copy(type = it)) },
            label = { Text("Тип (цемент, кирпич и т.д.)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Единица измерения
        OutlinedTextField(
            value = material.unit,
            onValueChange = { onMaterialChange(material.copy(unit = it)) },
            label = { Text("Единица измерения (кг, шт, м³)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Количество
        OutlinedTextField(
            value = material.quantity.toString(),
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                onMaterialChange(material.copy(quantity = value))
            },
            label = { Text("Количество") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Цена
        OutlinedTextField(
            value = material.price.toString(),
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                onMaterialChange(material.copy(price = value))
            },
            label = { Text("Цена за единицу (руб.)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Минимальный запас
        OutlinedTextField(
            value = material.minStockLevel.toString(),
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                onMaterialChange(material.copy(minStockLevel = value))
            },
            label = { Text("Минимальный запас") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Описание
        OutlinedTextField(
            value = material.description ?: "",
            onValueChange = { onMaterialChange(material.copy(description = it)) },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
}