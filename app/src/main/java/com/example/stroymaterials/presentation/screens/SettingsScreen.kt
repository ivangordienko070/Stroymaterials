// presentation/screens/SettingsScreen.kt
package com.example.stroymaterials.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stroymaterials.data.backup.BackupManager
import com.example.stroymaterials.data.database.AppDatabase
import com.example.stroymaterials.data.database.DatabaseSeeder
import com.example.stroymaterials.data.export.DataExporter
import com.example.stroymaterials.data.preferences.AppPreferences
import com.example.stroymaterials.presentation.navigation.Screen
import com.example.stroymaterials.presentation.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == "admin"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AppPreferences(context) }
    var isSeeding by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    var lowStockNotifications by remember { mutableStateOf(preferences.lowStockNotifications) }
    var criticalNotifications by remember { mutableStateOf(preferences.criticalNotifications) }
    var themeMode by remember { mutableStateOf(preferences.themeMode) }
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Настройки",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = false }
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Главное меню")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Уведомления
            SettingsCategory(
                title = "Уведомления",
                icon = Icons.Default.Notifications
            )

            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Уведомления о низком запасе",
                description = "Получать уведомления когда запас материалов низкий",
                checked = lowStockNotifications,
                onCheckedChange = {
                    lowStockNotifications = it
                    preferences.lowStockNotifications = it
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Warning,
                title = "Критические уведомления",
                description = "Уведомления о критически важных событиях",
                checked = criticalNotifications,
                onCheckedChange = {
                    criticalNotifications = it
                    preferences.criticalNotifications = it
                }
            )

            // Внешний вид
            SettingsCategory(
                title = "Внешний вид",
                icon = Icons.Default.Palette
            )

            SettingsThemeItem(
                icon = Icons.Default.Palette,
                title = "Тема приложения",
                description = "Светлая, темная или системная",
                currentTheme = themeMode,
                onThemeChange = {
                    themeMode = it
                    preferences.themeMode = it
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Тема изменена. Перезапустите приложение для применения.",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            )

            // Данные
            SettingsCategory(
                title = "Данные",
                icon = Icons.Default.Storage
            )

            if (isAdmin) {
                SettingsItem(
                    icon = Icons.Default.AddCircle,
                    title = "Заполнить базу данных",
                    description = "Заполнить базу данных тестовыми данными",
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    isLoading = isSeeding,
                    onClick = {
                        if (!isSeeding) {
                            isSeeding = true
                            scope.launch {
                                try {
                                    val database = AppDatabase.getDatabase(context)
                                    DatabaseSeeder.seedDatabase(database)
                                    snackbarHostState.showSnackbar(
                                        "База данных успешно заполнена тестовыми данными",
                                        duration = SnackbarDuration.Short
                                    )
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        "Ошибка заполнения базы данных: ${e.message}",
                                        duration = SnackbarDuration.Long
                                    )
                                } finally {
                                    isSeeding = false
                                }
                            }
                        }
                    }
                )
            }

            SettingsItem(
                icon = Icons.Default.FileDownload,
                title = "Экспорт данных",
                description = "Экспорт данных в CSV формате",
                iconColor = MaterialTheme.colorScheme.secondary,
                isLoading = isExporting,
                onClick = {
                    if (!isExporting) {
                        isExporting = true
                        scope.launch {
                            try {
                                val filePath = DataExporter.exportToCSV(context)
                                snackbarHostState.showSnackbar(
                                    "Данные экспортированы: $filePath",
                                    duration = SnackbarDuration.Long
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    "Ошибка экспорта: ${e.message}",
                                    duration = SnackbarDuration.Long
                                )
                            } finally {
                                isExporting = false
                            }
                        }
                    }
                }
            )

            if (isAdmin) {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Резервное копирование",
                    description = "Создать резервную копию данных",
                    iconColor = MaterialTheme.colorScheme.primary,
                    isLoading = isBackingUp,
                    onClick = {
                        if (!isBackingUp) {
                            isBackingUp = true
                            scope.launch {
                                try {
                                    val filePath = BackupManager.createBackup(context)
                                    snackbarHostState.showSnackbar(
                                        "Резервная копия создана: $filePath",
                                        duration = SnackbarDuration.Long
                                    )
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        "Ошибка создания резервной копии: ${e.message}",
                                        duration = SnackbarDuration.Long
                                    )
                                } finally {
                                    isBackingUp = false
                                }
                            }
                        }
                    }
                )
            }

            // О приложении
            SettingsCategory(
                title = "О приложении",
                icon = Icons.Default.Info
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Учет строительных материалов",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Версия 1.0.0",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Для учета и анализа поставок строительных материалов",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 24.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    isLoading: Boolean = false,
    onClick: () -> Unit = {}
) {
    val scale by animateFloatAsState(if (isLoading) 0.95f else 1f, label = "")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = iconColor
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val iconColor by animateColorAsState(
        if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = ""
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsThemeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val themes = listOf("light" to "Светлая", "dark" to "Темная", "system" to "Системная")
    val currentThemeLabel = themes.find { it.first == currentTheme }?.second ?: "Системная"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
            ) {
                OutlinedTextField(
                    value = currentThemeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Выберите тему") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    themes.forEach { (themeKey, themeLabel) ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    themeLabel,
                                    fontWeight = if (themeKey == currentTheme) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onThemeChange(themeKey)
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = if (themeKey == currentTheme) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}