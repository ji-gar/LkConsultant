package com.io.lkconsultants.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.lkconsultants.color.lkColors
import com.room.roomy.retrofit.LeaveRequest
import com.room.roomy.retrofit.Task
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeHomeScreen(onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val colors = lkColors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employee Portal", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = colors.primaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = colors.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Assignment, null) },
                    label = { Text("Tasks") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Leave") }
                )
            }
        },
        containerColor = colors.background
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                TaskListScreen()
            } else {
                LeaveRequestScreen()
            }
        }
    }
}

@Composable
fun TaskListScreen() {
    //
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = com.room.roomy.retrofit.RetrofitInstance.retrofits.getTasks()
            if (response.isSuccessful) {
                tasks = response.body()?.tasks ?: emptyList()
            } else {
                errorMessage = "Failed to load tasks: ${response.message()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = lkColors.primaryBlue)
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else if (tasks.isEmpty()) {
            Text(
                text = "No tasks assigned yet.",
                modifier = Modifier.align(Alignment.Center),
                color = lkColors.subtitle
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasks) { task ->
                    TaskItem(task)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestScreen() {
    var leaveType by remember { mutableStateOf("casual") }
    var dayType by remember { mutableStateOf("full") }
    var session by remember { mutableStateOf("first_half") }
    var reason by remember { mutableStateOf("") }
    
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var typeExpanded by remember { mutableStateOf(false) }
    var dayTypeExpanded by remember { mutableStateOf(false) }
    var sessionExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val leaveTypes = listOf("annual", "casual", "sick", "unpaid")
    val dayTypes = listOf("full", "half")
    val sessions = listOf("first_half", "second_half")

    // Leave list state
    var leaveList by remember { mutableStateOf<List<LeaveRequest>>(emptyList()) }
    var isLoadingList by remember { mutableStateOf(true) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    LaunchedEffect(Unit) {
        fetchLeaves { leaveList = it; isLoadingList = false }
    }

    if (showStartPicker) {
        MyDatePickerDialog(
            onDateSelected = { 
                startDate = it
                if (endDate.isBefore(it)) endDate = it
                showStartPicker = false 
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        MyDatePickerDialog(
            initialDate = endDate,
            onDateSelected = { endDate = it; showEndPicker = false },
            onDismiss = { showEndPicker = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Request Leave", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Form Fields
            Card(colors = CardDefaults.cardColors(containerColor = lkColors.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Leave Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = leaveType.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Leave Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            leaveTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                    onClick = { leaveType = type; typeExpanded = false }
                                )
                            }
                        }
                    }

                    // Day Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dayTypeExpanded,
                        onExpandedChange = { dayTypeExpanded = !dayTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = dayType.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayTypeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = dayTypeExpanded, onDismissRequest = { dayTypeExpanded = false }) {
                            dayTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                    onClick = { dayType = type; dayTypeExpanded = false }
                                )
                            }
                        }
                    }

                    if (dayType == "half") {
                        ExposedDropdownMenuBox(
                            expanded = sessionExpanded,
                            onExpandedChange = { sessionExpanded = !sessionExpanded }
                        ) {
                            OutlinedTextField(
                                value = session.replace("_", " ").replaceFirstChar { it.uppercase() },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Session") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sessionExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = sessionExpanded, onDismissRequest = { sessionExpanded = false }) {
                                sessions.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                                        onClick = { session = s; sessionExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Date Pickers
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate.format(dateFormatter),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = { IconButton(onClick = { showStartPicker = true }) { Icon(Icons.Default.DateRange, null) } }
                        )
                        if (dayType == "full") {
                            OutlinedTextField(
                                value = endDate.format(dateFormatter),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("End Date") },
                                modifier = Modifier.weight(1f),
                                trailingIcon = { IconButton(onClick = { showEndPicker = true }) { Icon(Icons.Default.DateRange, null) } }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )

                    Button(
                        onClick = {
                            val request = com.room.roomy.retrofit.ApplyLeaveRequest(
                                type = leaveType,
                                day_type = dayType,
                                from_date = startDate.format(dateFormatter),
                                to_date = if (dayType == "full") endDate.format(dateFormatter) else null,
                                session = if (dayType == "half") session else null,
                                reason = reason
                            )
                            scope.launch {
                                try {
                                    val response = com.room.roomy.retrofit.RetrofitInstance.retrofits.applyLeave(request)
                                    if (response.isSuccessful) {
                                        snackbarHostState.showSnackbar("Request submitted!")
                                        reason = ""
                                        fetchLeaves { leaveList = it }
                                    } else {
                                        snackbarHostState.showSnackbar("Failed: ${response.message()}")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.localizedMessage}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = reason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = lkColors.primaryBlue)
                    ) {
                        Text("Submit Request", color = Color.White)
                    }
                }
            }

            // Leave List Section
            Spacer(Modifier.height(8.dp))
            Text("Your Leave Requests", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            if (isLoadingList) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = lkColors.primaryBlue)
            } else if (leaveList.isEmpty()) {
                Text("No previous requests found.", color = lkColors.subtitle, fontSize = 14.sp)
            } else {
                leaveList.forEach { leave ->
                    LeaveItem(leave)
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task) {
    val colors = lkColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Icon + Title + Progress (Checklist)
            Row(verticalAlignment = Alignment.Top) {
                StatusIcon(task.status)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1.0f)) {
                    Text(
                        task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.onSurface
                    )
                    val completed = task.checklist?.count { it.done } ?: 0
                    val total = task.checklist?.size ?: 0
                    if (total > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Assignment,
                                null,
                                modifier = Modifier.size(12.dp),
                                tint = colors.subtitle
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("$completed/$total", color = colors.subtitle, fontSize = 12.sp)
                        }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = colors.divider)

            // Details Grid
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoColumn("Assignee", task.assignee?.name ?: "Unknown", task.assignee?.role ?: "")
                InfoColumn("Assigned by", task.assigner?.name ?: "System", task.assigner?.role ?: "")
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BadgeColumn("Status") { StatusBadge(task.status) }
                BadgeColumn("Priority") { PriorityBadge(task.priority) }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SimpleInfoColumn("Deadline", task.due_date.substringBefore("T"))
                SimpleInfoColumn("Started", task.started_at?.substringBefore("T") ?: "—")
                SimpleInfoColumn("Completed", task.completed_at?.substringBefore("T") ?: "—")
            }
        }
    }
}

@Composable
fun StatusIcon(status: String) {
    val color = when (status) {
        "completed" -> Color(0xFF4CAF50)
        "in_progress" -> Color(0xFF2196F3)
        else -> Color(0xFFFF9800)
    }
    val icon = when (status) {
        "completed" -> Icons.Default.CheckCircle
        "in_progress" -> Icons.Default.AccessTime
        else -> Icons.Default.ErrorOutline
    }
    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
}

@Composable
fun InfoColumn(label: String, name: String, role: String) {
    Column {
        Text(label, fontSize = 11.sp, color = lkColors.subtitle)
        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = lkColors.onSurface)
        Text(role.replaceFirstChar { it.uppercase() }, fontSize = 11.sp, color = lkColors.subtitle)
    }
}

@Composable
fun SimpleInfoColumn(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = lkColors.subtitle)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = lkColors.onSurface)
    }
}

@Composable
fun BadgeColumn(label: String, content: @Composable () -> Unit) {
    Column {
        Text(label, fontSize = 11.sp, color = lkColors.subtitle)
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "completed" -> Color(0xFF4CAF50)
        "in_progress" -> Color(0xFF2196F3)
        else -> Color(0xFFFF9800)
    }
    val displayText = status.replace("_", " ").replaceFirstChar { it.uppercase() }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (status == "completed") Icons.Default.CheckCircle else if (status == "in_progress") Icons.Default.AccessTime else Icons.Default.ErrorOutline,
                null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = displayText,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val color = when (priority) {
        "high" -> Color(0xFFE53935)
        "medium" -> Color(0xFFFFB300)
        "low" -> Color(0xFF43A047)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = priority.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LeaveItem(leave: LeaveRequest) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = lkColors.surfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, lkColors.divider.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(leave.type.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, color = lkColors.onSurface)
                LeaveStatusBadge(leave.status)
            }
            Spacer(Modifier.height(4.dp))
            val dateRange = if (leave.day_type == "full") "${leave.from_date.substringBefore("T")} to ${leave.to_date.substringBefore("T")}" else leave.from_date.substringBefore("T")
            Text(dateRange, fontSize = 12.sp, color = lkColors.subtitle)
            Text("${leave.days} day(s) • ${leave.day_type}", fontSize = 11.sp, color = lkColors.subtitle)
            if (leave.reason.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(leave.reason, fontSize = 13.sp, color = lkColors.onSurface, maxLines = 2)
            }
        }
    }
}

@Composable
fun LeaveStatusBadge(status: String) {
    val color = when (status) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFE53935)
        else -> Color(0xFFFF9800)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun fetchLeaves(onResult: (List<LeaveRequest>) -> Unit) {
    kotlinx.coroutines.MainScope().launch {
        try {
            val response = com.room.roomy.retrofit.RetrofitInstance.retrofits.listLeaves()
            if (response.isSuccessful) {
                onResult(response.body()?.leave_requests ?: emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
