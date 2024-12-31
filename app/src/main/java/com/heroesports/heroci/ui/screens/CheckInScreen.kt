package com.heroesports.heroci.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.heroesports.heroci.data.entity.CheckIn
import com.heroesports.heroci.data.entity.Member
import com.heroesports.heroci.ui.viewmodel.CheckInViewModel
import com.heroesports.heroci.utils.getLocation
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    projectId: Long,
    viewModel: CheckInViewModel,
    onNavigateBack: () -> Unit
) {
    val members by viewModel.getMembers(projectId).collectAsState(initial = emptyList())
    val checkIns by viewModel.getCheckIns(projectId).collectAsState(initial = emptyList())
    val project by viewModel.currentProject.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    var showCheckInForm by remember { mutableStateOf(false) }

    // 获取今天的打卡记录
    val todayCheckIns = checkIns.filter { checkIn -> 
        checkIn.checkInTime.toLocalDate() == LocalDateTime.now().toLocalDate()
    }

    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCheckInForm = true },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("打卡")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "今日打卡状态",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(members) { member ->
                    val todayCheckIn = todayCheckIns.find { it.memberName == member.name }

                    ListItem(
                        headlineContent = { Text(member.name) },
                        supportingContent = {
                            if (todayCheckIn != null) {
                                Text(
                                    "已打卡 - ${todayCheckIn.checkInTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    "未打卡",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    Divider()
                }
            }
        }

        if (showCheckInForm) {
            CheckInDialog(
                members = members,
                todayCheckIns = todayCheckIns,
                context = context,
                geocoder = geocoder,
                onDismiss = { showCheckInForm = false },
                onSubmit = { memberName, location, photoUri, latitude, longitude ->
                    viewModel.checkIn(
                        projectId = projectId,
                        memberName = memberName,
                        location = location,
                        photoPath = photoUri.toString(),
                        latitude = latitude,
                        longitude = longitude
                    )
                    showCheckInForm = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInDialog(
    members: List<Member>,
    todayCheckIns: List<CheckIn>,
    context: Context,
    geocoder: Geocoder,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Uri, Double, Double) -> Unit
) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val file = remember { File(context.cacheDir, "${System.currentTimeMillis()}.jpg") }
    val uri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            scope.launch {
                getLocation(
                    context,
                    geocoder,
                    onLocationResult = { address, lat, lng ->
                        location = address
                        latitude = lat
                        longitude = lng
                    },
                    onError = { error ->
                        locationError = error
                    }
                )
            }
        } else {
            locationError = "需要定位权限才能打卡"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = uri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(uri)
            }
            else -> {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    fun requestLocation() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        locationPermissionLauncher.launch(permissions)
    }

    fun checkAndSubmit() {
        selectedMember?.let { member ->
            val hasCheckedIn = todayCheckIns.any { checkIn -> checkIn.memberName == member.name }
            if (hasCheckedIn) {
                showConfirmDialog = true
            } else {
                photoUri?.let { uri ->
                    onSubmit(member.name, location, uri, latitude, longitude)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("重复打卡确认") },
            text = { Text("今天已经打过卡了，是否要覆盖打卡记录？\n注意：这将更新位置和照片信息。") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        photoUri?.let { uri ->
                            selectedMember?.let { member ->
                                onSubmit(member.name, location, uri, latitude, longitude)
                            }
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("打卡") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // 选择人员
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMember?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("选择人员") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedMember = member
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 定位按钮
                OutlinedButton(
                    onClick = { 
                        isGettingLocation = true
                        locationError = null
                        requestLocation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGettingLocation
                ) {
                    if (isGettingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("正在获取位置...")
                    } else {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (location.isEmpty()) "获取位置" else location)
                    }
                }

                locationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    TextButton(
                        onClick = {
                            locationError = null
                            isGettingLocation = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重试")
                    }
                }

                LaunchedEffect(location) {
                    if (location.isNotEmpty()) {
                        isGettingLocation = false
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 拍照按钮
                OutlinedButton(
                    onClick = { launchCamera() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (photoUri == null) "拍照" else "已拍照")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { checkAndSubmit() },
                enabled = selectedMember != null && location.isNotEmpty() && photoUri != null
            ) {
                Text("提交")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 