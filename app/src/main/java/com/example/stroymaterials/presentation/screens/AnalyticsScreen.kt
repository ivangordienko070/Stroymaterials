// presentation/screens/AnalyticsScreen.kt
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
import com.example.stroymaterials.data.database.AppDatabase
import com.example.stroymaterials.data.repositories.DeliveryRepository
import com.example.stroymaterials.data.repositories.MaterialRepository
import com.example.stroymaterials.data.repositories.SupplierRepository
import com.example.stroymaterials.presentation.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val materialRepo = MaterialRepository(database.materialDao())
    val supplierRepo = SupplierRepository(database.supplierDao())
    val deliveryRepo = DeliveryRepository(database.deliveryDao())
    
    var materialsCount by remember { mutableStateOf(0) }
    var suppliersCount by remember { mutableStateOf(0) }
    var deliveriesCount by remember { mutableStateOf(0) }
    var totalInventoryValue by remember { mutableStateOf(0.0) }
    var pendingDeliveriesCount by remember { mutableStateOf(0) }
    var deliveredDeliveriesCount by remember { mutableStateOf(0) }
    var totalCostThisMonth by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                materialsCount = materialRepo.getMaterialsCount()
                suppliersCount = supplierRepo.getSuppliersCount()
                deliveriesCount = deliveryRepo.getTotalDeliveriesCount()
                totalInventoryValue = materialRepo.getTotalInventoryValue() ?: 0.0
                pendingDeliveriesCount = deliveryRepo.getPendingDeliveriesCount()
                deliveredDeliveriesCount = deliveryRepo.getDeliveredDeliveriesCount()
                
                // Получаем стоимость поставок за текущий месяц
                val calendar = java.util.Calendar.getInstance()
                val startOfMonth = calendar.apply {
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time
                val endOfMonth = java.util.Calendar.getInstance().time
                totalCostThisMonth = deliveryRepo.getTotalCostForPeriod(startOfMonth, endOfMonth) ?: 0.0
                
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = false }
                        }
                    }) {
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
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Общая статистика
                Text(
                    text = "Общая статистика",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Материалы",
                        value = materialsCount.toString(),
                        icon = Icons.Default.Inventory,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.MaterialList.route) }
                    )
                    StatCard(
                        title = "Поставщики",
                        value = suppliersCount.toString(),
                        icon = Icons.Default.Business,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.SupplierList.route) }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Поставки",
                        value = deliveriesCount.toString(),
                        icon = Icons.Default.LocalShipping,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.DeliveryList.route) }
                    )
                    StatCard(
                        title = "Ожидается",
                        value = pendingDeliveriesCount.toString(),
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.DeliveryList.route) }
                    )
                }
                
                Divider()
                
                // Финансовая статистика
                Text(
                    text = "Финансы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                StatCard(
                    title = "Стоимость запасов",
                    value = String.format("%.2f", totalInventoryValue),
                    subtitle = "руб.",
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Screen.MaterialList.route) }
                )
                
                StatCard(
                    title = "Стоимость поставок (месяц)",
                    value = String.format("%.2f", totalCostThisMonth),
                    subtitle = "руб.",
                    icon = Icons.Default.Payments,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Screen.DeliveryList.route) }
                )
                
                Divider()
                
                // Статистика по поставкам
                Text(
                    text = "Статистика поставок",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                StatCard(
                    title = "Доставлено",
                    value = deliveredDeliveriesCount.toString(),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(Screen.DeliveryList.route) }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

