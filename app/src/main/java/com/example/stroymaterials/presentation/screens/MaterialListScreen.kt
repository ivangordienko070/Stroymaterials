// presentation/screens/MaterialListScreen.kt
package com.example.stroymaterials.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.presentation.components.SearchBar
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.AuthViewModel
import com.example.stroymaterials.presentation.viewmodels.MaterialViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialListScreen(
    navController: NavController,
    viewModel: MaterialViewModel,
    authViewModel: AuthViewModel,
    onMaterialClick: (Long) -> Unit,
    onAddMaterial: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "admin"
    val materials by viewModel.materials.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Загружаем материалы при первом запуске
    LaunchedEffect(Unit) {
        viewModel.loadMaterials()
    }
    
    // Обрабатываем поиск
    LaunchedEffect(searchQuery.value) {
        viewModel.searchMaterials(searchQuery.value)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Строительные материалы") },
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
                FloatingActionButton(onClick = onAddMaterial) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить материал")
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
                modifier = Modifier.padding(16.dp)
            )

            // Список материалов
            if (materials.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет материалов")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(materials) { material ->
                        MaterialCard(
                            material = material,
                            onClick = { onMaterialClick(material.id) },
                            onEditClick = if (isAdmin) {
                                { navController.navigate(Screen.MaterialEdit.createRoute(material.id)) }
                            } else null,
                            onDeleteClick = if (isAdmin) {
                                {
                                    scope.launch {
                                        try {
                                            viewModel.deleteMaterial(material)
                                            snackbarHostState.showSnackbar("Материал удален")
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
fun MaterialCard(
    material: MaterialEntity,
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
                    text = material.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!material.isActive) {
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Тип: ${material.type}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Количество: ${String.format("%.2f", material.quantity)} ${material.unit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.2f", material.price)} руб./${material.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Итого: ${String.format("%.2f", material.quantity * material.price)} руб.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Показываем предупреждение о низком запасе
            if (material.quantity <= material.minStockLevel) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠ Низкий запас! Минимум: ${String.format("%.2f", material.minStockLevel)} ${material.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}