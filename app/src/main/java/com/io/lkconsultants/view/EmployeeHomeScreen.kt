package com.io.lkconsultants.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.io.lkconsultants.color.lkColors
import com.room.roomy.retrofit.*
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
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LKEDC EMS", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Surface(
                            color = colors.primaryBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (selectedTab == 0) "TASK MANAGEMENT" else "LEAVE PORTAL",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.primaryBlue,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = colors.brandRed)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment, null) },
                    label = { Text("Tasks") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primaryBlue,
                        selectedTextColor = colors.primaryBlue,
                        indicatorColor = colors.primaryBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Leaves") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primaryBlue,
                        selectedTextColor = colors.primaryBlue,
                        indicatorColor = colors.primaryBlue.copy(alpha = 0.1f)
                    )
                )
            }
        },
        containerColor = colors.background
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
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
    var taskResponse by remember { mutableStateOf<TaskListResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    fun loadTasks() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitInstance.retrofits.listTasks(
                    search = searchQuery.takeIf { it.isNotBlank() },
                    status = selectedStatus
                )
                if (response.isSuccessful) {
                    taskResponse = response.body()
                } else {
                    errorMessage = "Session Expired or Server Error"
                }
            } catch (e: Exception) {
                errorMessage = "Network Error"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery, selectedStatus) {
        loadTasks()
    }

    Column(Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search tasks..."
        )

        FilterRow(
            options = listOf("All", "Pending", "In Progress", "Completed"),
            selected = when (selectedStatus) {
                "pending" -> "Pending"
                "in_progress" -> "In Progress"
                "completed" -> "Completed"
                else -> "All"
            },
            onSelect = {
                selectedStatus = when (it) {
                    "Pending" -> "pending"
                    "In Progress" -> "in_progress"
                    "Completed" -> "completed"
                    else -> null
                }
            }
        )

        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = lkColors.primaryBlue)
        }

        if (errorMessage != null) {
            ErrorPlaceholder(errorMessage!!) { 
                errorMessage = null
                loadTasks()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                taskResponse?.counts?.let { counts ->
                    item {
                        TaskSummaryRow(counts)
                    }
                }

                val tasks = taskResponse?.tasks ?: emptyList()
                if (tasks.isEmpty() && !isLoading) {
                    item {
                        EmptyPlaceholder("No tasks found.")
                    }
                } else {
                    items(tasks) { task ->
                    TaskItem(task, onAction = { loadTasks() })
                }
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = lkColors.subtitle) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, null, tint = lkColors.subtitle)
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = lkColors.surface,
            focusedContainerColor = lkColors.surface,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = lkColors.primaryBlue
        )
    )
}

@Composable
fun FilterRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(options) { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = { Text(option, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = lkColors.primaryBlue,
                    selectedLabelColor = Color.White
                ),
                border = if (!isSelected) BorderStroke(1.dp, lkColors.divider) else null
            )
        }
    }
}

@Composable
fun TaskSummaryRow(counts: TaskCounts) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(Modifier.weight(1f), "Today", counts.today.toString(), lkColors.primaryBlue, Icons.Default.Today)
        SummaryCard(Modifier.weight(1f), "Live", (counts.pending + counts.in_progress).toString(), Color(0xFFFF9800), Icons.Default.Bolt)
        SummaryCard(Modifier.weight(1f), "Done", counts.completed.toString(), Color(0xFF4CAF50), Icons.Default.CheckCircle)
    }
}

@Composable
fun SummaryCard(modifier: Modifier, label: String, value: String, color: Color, icon: ImageVector) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = lkColors.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = lkColors.onSurface)
            Text(label, fontSize = 10.sp, color = lkColors.subtitle, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TaskItem(task: Task, onAction: () -> Unit) {
    val colors = lkColors
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colors.divider.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    StatusIcon(task.status)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            task.title, 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 15.sp, 
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        PriorityBadge(task.priority)
                        Spacer(Modifier.width(4.dp))
                        ApprovalStatusBadge(task.approval_status)
                    }
                    task.description?.let { 
                        Text(it, fontSize = 12.sp, color = colors.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            val total = task.checklist?.size ?: 0
            val completed = task.checklist?.count { it.done } ?: 0
            if (total > 0) {
                TaskProgressIndicator(completed, total)
                Spacer(Modifier.height(16.dp))
            }

            Row(
                Modifier.fillMaxWidth().background(colors.background, RoundedCornerShape(12.dp)).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserMiniProfile("Assigner", task.assigner)
                DeadlineBadge(task.due_date)
            }

            // Task Actions
            if (task.status != "completed" && task.approval_status != "pending" && task.approval_status != "rejected") {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (task.status == "pending") {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val res = RetrofitInstance.retrofits.updateTaskStatus(task.id, UpdateStatusRequest("in_progress"))
                                        if (res.isSuccessful) onAction()
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue)
                        ) {
                            Text("Start Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (task.status == "in_progress") {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val res = RetrofitInstance.retrofits.completeTask(task.id)
                                        if (res.isSuccessful) onAction()
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Complete Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskProgressIndicator(completed: Int, total: Int) {
    val progress = completed.toFloat() / total
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progress", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = lkColors.subtitle)
            Text("${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = lkColors.primaryBlue)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = lkColors.primaryBlue,
            trackColor = lkColors.divider
        )
    }
}

@Composable
fun UserMiniProfile(label: String, user: UserBrief?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(lkColors.primaryBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Text(user?.name?.take(1)?.uppercase() ?: "S", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = lkColors.primaryBlue)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 8.sp, color = lkColors.subtitle, fontWeight = FontWeight.Bold)
            Text(user?.name ?: "System", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = lkColors.onSurface)
        }
    }
}

@Composable
fun DeadlineBadge(date: String) {
    Surface(
        color = lkColors.brandRed.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, null, modifier = Modifier.size(10.dp), tint = lkColors.brandRed)
            Spacer(Modifier.width(4.dp))
            Text(date.substringBefore("T"), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = lkColors.brandRed)
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

    var leaveResponse by remember { mutableStateOf<LeaveListResponse?>(null) }
    var policyResponse by remember { mutableStateOf<LeavePolicyResponse?>(null) }
    var holidayResponse by remember { mutableStateOf<HolidayResponse?>(null) }
    var isLoadingList by remember { mutableStateOf(true) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun loadLeaves() {
        isLoadingList = true
        scope.launch {
            try {
                val response = RetrofitInstance.retrofits.listLeaves()
                if (response.isSuccessful) leaveResponse = response.body()
                
                val pResponse = RetrofitInstance.retrofits.getLeavePolicy()
                if (pResponse.isSuccessful) {
                    policyResponse = pResponse.body()
                    // Initialize leaveType with first policy name if not already set
                    if (leaveType.isEmpty() || leaveType == "casual") {
                        policyResponse?.policies?.firstOrNull { it.remaining > 0 }?.let {
                            leaveType = it.name
                        }
                    }
                }

                val hResponse = RetrofitInstance.retrofits.getHolidays()
                if (hResponse.isSuccessful) holidayResponse = hResponse.body()
                
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoadingList = false }
        }
    }

    LaunchedEffect(Unit) { loadLeaves() }

    val holidays = holidayResponse?.holidays?.map { it.date } ?: emptyList()
    val minDate = policyResponse?.policies?.find { it.name == leaveType }?.let { 
        LocalDate.now().plusDays(it.min_notice_days.toLong()) 
    }

    if (showStartPicker) {
        MyDatePickerDialog(
            date = startDate, 
            minDate = minDate,
            holidays = holidays,
            onSel = { 
                startDate = it
                if (endDate.isBefore(it)) endDate = it
                showStartPicker = false 
            }, 
            onDim = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        MyDatePickerDialog(
            date = endDate, 
            minDate = startDate, // End date cannot be before start date
            holidays = holidays,
            onSel = { 
                endDate = it
                showEndPicker = false 
            }, 
            onDim = { showEndPicker = false }
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
            SectionHeader("Apply for Leave")

            Card(
                colors = CardDefaults.cardColors(containerColor = lkColors.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, lkColors.divider.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Type & Day Type Row
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DropdownSelector(
                            Modifier.weight(1f), 
                            "Type", 
                            leaveType.replaceFirstChar { it.uppercase() }, 
                            typeExpanded, 
                            { typeExpanded = it }
                        ) {
                            policyResponse?.policies?.forEach { p ->
                                val isAvailable = p.remaining > 0
                                DropdownMenuItem(
                                    text = { 
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(p.name)
                                            if (!isAvailable) {
                                                Text(" (Exhausted)", fontSize = 10.sp, color = lkColors.brandRed)
                                            } else {
                                                Text(" (${p.remaining.toInt()} left)", fontSize = 10.sp, color = lkColors.primaryBlue)
                                            }
                                        }
                                    }, 
                                    onClick = { 
                                        leaveType = p.name
                                        typeExpanded = false 
                                    },
                                    enabled = isAvailable
                                )
                            }
                            if (policyResponse == null) {
                                listOf("annual", "casual", "sick", "unpaid").forEach { t ->
                                    DropdownMenuItem(text = { Text(t.replaceFirstChar { it.uppercase() }) }, onClick = { leaveType = t; typeExpanded = false })
                                }
                            }
                        }
                        DropdownSelector(Modifier.weight(1f), "Day", dayType.replaceFirstChar { it.uppercase() }, dayTypeExpanded, { dayTypeExpanded = it }) {
                            listOf("full", "half").forEach { t ->
                                DropdownMenuItem(text = { Text(t.replaceFirstChar { it.uppercase() }) }, onClick = { dayType = t; dayTypeExpanded = false })
                            }
                        }
                    }

                    if (dayType == "half") {
                        DropdownSelector(Modifier.fillMaxWidth(), "Session", session.replace("_", " ").replaceFirstChar { it.uppercase() }, sessionExpanded, { sessionExpanded = it }) {
                            listOf("first_half", "second_half").forEach { s ->
                                DropdownMenuItem(text = { Text(s.replace("_", " ").replaceFirstChar { it.uppercase() }) }, onClick = { session = s; sessionExpanded = false })
                            }
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DatePickerField(Modifier.weight(1f), "From", startDate.format(dateFormatter)) { showStartPicker = true }
                        if (dayType == "full") {
                            DatePickerField(Modifier.weight(1f), "To", endDate.format(dateFormatter)) { showEndPicker = true }
                        }
                    }

                    OutlinedTextField(
                        value = reason, onValueChange = { reason = it },
                        label = { Text("Reason", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = lkColors.divider)
                    )

                    Button(
                        onClick = {
                            val request = ApplyLeaveRequest(leaveType, dayType, startDate.format(dateFormatter), if (dayType == "full") endDate.format(dateFormatter) else null, if (dayType == "half") session else null, reason)
                            scope.launch {
                                try {
                                    val res = RetrofitInstance.retrofits.applyLeave(request)
                                    if (res.isSuccessful) { snackbarHostState.showSnackbar("Success!"); reason = ""; loadLeaves() }
                                } catch (e: Exception) { snackbarHostState.showSnackbar("Error") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = lkColors.primaryBlue),
                        enabled = reason.isNotBlank()
                    ) { Text("Submit Application", fontWeight = FontWeight.ExtraBold) }
                }
            }

            leaveResponse?.counts?.let { LeaveSummaryGrid(it) }

            policyResponse?.let { PolicySummaryGrid(it.policies) }

            SectionHeader("History")
            
            if (isLoadingList) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = lkColors.primaryBlue)
            } else {
                leaveResponse?.leave_requests?.forEach { LeaveItem(it) }
            }
        }
    }
}

@Composable
fun PolicySummaryGrid(policies: List<LeavePolicy>) {
    SectionHeader("Policy Status")
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(policies) { policy ->
            PolicyCard(policy)
        }
    }
}

@Composable
fun PolicyCard(policy: LeavePolicy) {
    Card(
        colors = CardDefaults.cardColors(containerColor = lkColors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, lkColors.divider.copy(alpha = 0.3f)),
        modifier = Modifier.width(140.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(policy.name, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = lkColors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Used", fontSize = 9.sp, color = lkColors.subtitle)
                    Text(policy.used.toInt().toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = lkColors.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Left", fontSize = 9.sp, color = lkColors.subtitle)
                    Text(policy.remaining.toInt().toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = lkColors.primaryBlue)
                }
            }
            Spacer(Modifier.height(8.dp))
            val progress = if (policy.count > 0) (policy.used / policy.count).toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = if (policy.remaining <= 0) lkColors.brandRed else lkColors.primaryBlue,
                trackColor = lkColors.divider
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(16.dp).background(lkColors.primaryBlue, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = lkColors.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(modifier: Modifier, label: String, value: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange, modifier = modifier) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true,
            label = { Text(label, fontSize = 10.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = lkColors.divider)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, content = content)
    }
}

@Composable
fun DatePickerField(modifier: Modifier, label: String, value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = {}, readOnly = true,
        label = { Text(label, fontSize = 10.sp) },
        trailingIcon = { Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp).clickable { onClick() }, tint = lkColors.primaryBlue) },
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = lkColors.divider)
    )
}

@Composable
fun LeaveSummaryGrid(counts: LeaveCounts) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LeaveSummaryMiniCard(Modifier.weight(1f), "Approved", counts.approved.toString(), Color(0xFF4CAF50))
        LeaveSummaryMiniCard(Modifier.weight(1f), "Pending", counts.pending.toString(), Color(0xFFFF9800))
        LeaveSummaryMiniCard(Modifier.weight(1f), "Rejected", counts.rejected.toString(), lkColors.brandRed)
    }
}

@Composable
fun LeaveSummaryMiniCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = lkColors.subtitle)
        }
    }
}

@Composable
fun LeaveItem(leave: LeaveRequest) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = lkColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, lkColors.divider.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(leave.type.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val dateStr = if (leave.day_type == "full") {
                    "${leave.from_date.substringBefore("T")} - ${leave.to_date.substringBefore("T")}"
                } else {
                    "${leave.from_date.substringBefore("T")} (${leave.session?.replace("_", " ")})"
                }
                Text(dateStr, fontSize = 11.sp, color = lkColors.subtitle)
                Text("${leave.days} Day(s)", fontSize = 11.sp, color = lkColors.primaryBlue, fontWeight = FontWeight.Bold)
            }
            LeaveStatusBadge(leave.status)
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
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun StatusIcon(status: String) {
    val (icon, color) = when (status) {
        "completed" -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        "in_progress" -> Icons.Default.Sync to Color(0xFF2196F3)
        else -> Icons.Default.Circle to Color(0xFFFF9800).copy(alpha = 0.3f)
    }
    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
}

@Composable
fun PriorityBadge(priority: String) {
    val color = when (priority) {
        "high" -> lkColors.brandRed
        "medium" -> Color(0xFFFFB300)
        else -> Color(0xFF4CAF50)
    }
    Text(
        priority.uppercase(),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
        color = color, fontSize = 8.sp, fontWeight = FontWeight.Black
    )
}

@Composable
fun ApprovalStatusBadge(status: String) {
    val color = when (status) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> lkColors.brandRed
        "pending" -> Color(0xFFFF9800)
        else -> lkColors.subtitle
    }
    if (status != "not_required") {
        Text(
            status.replace("_", " ").uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
            color = color, fontSize = 8.sp, fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun ErrorPlaceholder(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), tint = lkColors.brandRed)
            Spacer(Modifier.height(16.dp))
            Text(msg, fontWeight = FontWeight.Bold, color = lkColors.onSurface)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
fun EmptyPlaceholder(msg: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, null, Modifier.size(48.dp), tint = lkColors.divider)
            Spacer(Modifier.height(8.dp))
            Text(msg, color = lkColors.subtitle, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    date: LocalDate, 
    minDate: LocalDate? = null, 
    holidays: List<String> = emptyList(),
    onSel: (LocalDate) -> Unit, 
    onDim: () -> Unit
) {
    val selectableDates = remember(minDate, holidays) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val dateAtUtc = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                
                // Min Date Check
                if (minDate != null && dateAtUtc.isBefore(minDate)) return false
                
                // Sunday Check
                if (dateAtUtc.dayOfWeek == java.time.DayOfWeek.SUNDAY) return false
                
                // Holiday Check
                val dateStr = dateAtUtc.format(DateTimeFormatter.ISO_LOCAL_DATE) // yyyy-MM-dd
                if (holidays.any { it.startsWith(dateStr) }) return false
                
                return true
            }
        }
    }
    
    val state = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        selectableDates = selectableDates
    )
    DatePickerDialog(
        onDismissRequest = onDim, 
        confirmButton = { 
            TextButton(onClick = { 
                state.selectedDateMillis?.let { 
                    onSel(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()) 
                } 
            }) { Text("OK") } 
        }, 
        dismissButton = { 
            TextButton(onClick = onDim) { Text("Cancel") } 
        }
    ) { 
        DatePicker(state) 
    }
}
