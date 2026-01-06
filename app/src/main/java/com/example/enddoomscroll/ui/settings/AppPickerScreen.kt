package com.example.enddoomscroll.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.enddoomscroll.data.database.AppDatabase
import com.example.enddoomscroll.data.entity.AppSettings
import com.example.enddoomscroll.data.repository.AppUsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember {
        AppUsageRepository(
            database.appUsageDao(),
            database.appSettingsDao(),
            database.challengeHistoryDao()
        )
    }

    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var monitoredPackages by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(0)

            // 🔧 FIX: explicitly handle nullable ApplicationInfo
            val apps = packages.mapNotNull { pkgInfo ->
                val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null
                val appName = pm.getApplicationLabel(appInfo).toString()
                AppInfo(
                    packageName = pkgInfo.packageName,
                    name = appName
                )
            }.sortedBy { it.name.lowercase() }

            installedApps = apps

            val settings = repository.getAllSettings().first()
            monitoredPackages =
                settings.filter { it.isMonitored }.map { it.packageName }.toSet()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Monitored Apps") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(installedApps) { appInfo ->
                val isMonitored = monitoredPackages.contains(appInfo.packageName)

                AppToggleItem(
                    appName = appInfo.name,
                    packageName = appInfo.packageName,
                    isMonitored = isMonitored,
                    onToggle = { enabled ->
                        scope.launch(Dispatchers.IO) {
                            val existing =
                                repository.getSettingsForPackage(appInfo.packageName)

                            if (enabled) {
                                val settings = existing ?: AppSettings(
                                    packageName = appInfo.packageName,
                                    appName = appInfo.name
                                )
                                repository.insertOrUpdateSettings(
                                    settings.copy(isMonitored = true)
                                )
                            } else {
                                if (existing != null) {
                                    repository.insertOrUpdateSettings(
                                        existing.copy(isMonitored = false)
                                    )
                                }
                            }

                            val refreshed =
                                repository.getAllSettings().first()
                            monitoredPackages =
                                refreshed.filter { it.isMonitored }
                                    .map { it.packageName }
                                    .toSet()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppToggleItem(
    appName: String,
    packageName: String,
    isMonitored: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isMonitored,
                onCheckedChange = onToggle
            )
        }
    }
}

private data class AppInfo(
    val packageName: String,
    val name: String
)

