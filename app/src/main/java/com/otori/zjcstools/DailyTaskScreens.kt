package com.otori.zjcstools

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
@Composable
fun HomeScreen(
    onTaskModeClick: () -> Unit,
    onPersonModeClick: () -> Unit,
    onManageClick: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(20.dp)
        ){
            Spacer(modifier = Modifier.height(80.dp))

            StrokeText(
                text = "日常记录",
                fontSize = 40,
                fillColor = Color.White,
                strokeColor = Color(0xFF202020),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            StrokeText(
                text = "内容 × 角色 双记录模式",
                fontSize = 24,
                fillColor = Color.White,
                strokeColor = Color(0xFF202020)
            )

            Spacer(modifier = Modifier.height(60.dp))

            HomeCardButton(
                title = "游戏内容模式",
                subtitle = "按游戏内容查看每个角色的完成情况",
                onClick = onTaskModeClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            HomeCardButton(
                title = "角色名单模式",
                subtitle = "按角色名查看每个游戏内容的完成情况",
                onClick = onPersonModeClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            HomeCardButton(
                title = "管理",
                subtitle = "新增或删除角色和内容",
                onClick = onManageClick
            )
        }

        TopBackButton(onBack = onBack)
    }
}

@Composable
fun TaskModeScreen(
    tasks: List<TaskItem>,
    persons: List<String>,
    checkedMap: MutableMap<String, Boolean>,
    disabledRoleMap: Map<String, Boolean>,
    onCheckedChange: (String, Boolean) -> Unit,
    onResetAll: () -> Unit,
    onBack: () -> Unit
) {
    var expandedTask by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF6F7FB))
            .safeDrawingPadding()
            .padding(20.dp)
    ) {
        TopTitleWithReset(
            title = "游戏内容模式",
            onBack = onBack,
            onResetAll = onResetAll
        )

        Spacer(modifier = Modifier.height(20.dp))

        tasks.forEach { task ->
            val enabledPersons = persons.filter { person ->
                isRoleEnabled(disabledRoleMap, person, task.name)
            }
            val finishedCount = enabledPersons.count { person ->
                checkedMap[checkedKey(person, task.name)] == true
            }

            ProgressCard(
                title = "${task.name} · ${resetText(task)}",
                progress = "$finishedCount/${enabledPersons.size}",
                completed = (
                        enabledPersons.isNotEmpty() &&
                                finishedCount == enabledPersons.size
                        ),
                onClick = {
                    expandedTask = if (expandedTask == task.name) null else task.name
                }
            )

            if (expandedTask == task.name) {
                enabledPersons.forEach { person ->
                    val key = checkedKey(person, task.name)
                    val checked = checkedMap[key] ?: false

                    CheckRow(
                        text = person,
                        checked = checked,
                        onCheckedChange = {
                            onCheckedChange(key, it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PersonModeScreen(
    tasks: List<TaskItem>,
    persons: List<String>,
    checkedMap: MutableMap<String, Boolean>,
    disabledRoleMap: Map<String, Boolean>,
    onCheckedChange: (String, Boolean) -> Unit,
    onResetAll: () -> Unit,
    onBack: () -> Unit
) {
    var expandedPerson by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF6F7FB))
            .safeDrawingPadding()
            .padding(20.dp)
    ) {
        TopTitleWithReset(
            title = "角色名单模式",
            onBack = onBack,
            onResetAll = onResetAll
        )

        Spacer(modifier = Modifier.height(20.dp))

        persons.forEach { person ->
            val enabledTasks = tasks.filter { task ->
                isRoleEnabled(disabledRoleMap, person, task.name)
            }
            val finishedCount = enabledTasks.count { task ->
                checkedMap[checkedKey(person, task.name)] == true
            }

            ProgressCard(
                title = person,
                progress = "$finishedCount/${enabledTasks.size}",
                completed = (
                        enabledTasks.isNotEmpty() &&
                                finishedCount == enabledTasks.size
                        ),
                onClick = {
                    expandedPerson = if (expandedPerson == person) null else person
                }
            )

            if (expandedPerson == person) {
                enabledTasks.forEach { task ->
                    val key = checkedKey(person, task.name)
                    val checked = checkedMap[key] ?: false

                    CheckRow(
                        text = "${task.name} · ${resetText(task)}",
                        checked = checked,
                        onCheckedChange = {
                            onCheckedChange(key, it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ManageScreen(
    tasks: MutableList<TaskItem>,
    persons: MutableList<String>,
    checkedMap: MutableMap<String, Boolean>,
    disabledRoleMap: MutableMap<String, Boolean>,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var newPerson by remember { mutableStateOf("") }
    var newTask by remember { mutableStateOf("") }
    var newTaskResetType by remember { mutableStateOf(RESET_TYPE_DAILY) }
    var newTaskResetDay by remember { mutableIntStateOf(1) }
    var isReordering by remember { mutableStateOf(false) }
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }
    var expandedPersonName by remember { mutableStateOf<String?>(null) }
    var expandedTaskName by remember { mutableStateOf<String?>(null) }
    val manageScrollState = rememberScrollState()
    val density = LocalDensity.current
    val screenHeightPx = with(density) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val edgeScrollAreaPx = with(density) { 96.dp.toPx() }

    LaunchedEffect(isReordering, autoScrollSpeed) {
        while (isReordering && autoScrollSpeed != 0f) {
            manageScrollState.scrollBy(autoScrollSpeed)
            delay(16.milliseconds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(manageScrollState, enabled = !isReordering)
            .background(Color(0xFFF6F7FB))
            .safeDrawingPadding()
            .padding(20.dp)
    ) {
        TopTitle(title = "管理", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "管理角色",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "可展开列表给任务分配角色",
            fontSize = 14.sp,
            color = Color(0xFF777777)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AddRow(
            value = newPerson,
            label = "输入角色名",
            onValueChange = { newPerson = it },
            onAdd = {
                val personName = newPerson.trim()

                if (
                    personName.isNotBlank() &&
                    !containsReservedSeparator(personName) &&
                    persons.none { it == personName }
                ) {
                    persons.add(personName)
                    newPerson = ""
                    onSave()
                }
            }
        )

        ReorderableDeleteList(
            items = persons,
            itemKey = { it },
            itemText = { it },
            canExpand = { tasks.isNotEmpty() },
            isExpanded = { tasks.isNotEmpty() && expandedPersonName == it },
            onRowClick = { person ->
                if (tasks.isNotEmpty()) {
                    expandedPersonName = if (expandedPersonName == person) {
                        null
                    } else {
                        person
                    }
                }
            },
            onDragStateChange = {
                isReordering = it
                if (!it) {
                    autoScrollSpeed = 0f
                }
            },
            screenHeightPx = screenHeightPx,
            edgeScrollAreaPx = edgeScrollAreaPx,
            onAutoScrollSpeedChange = {
                autoScrollSpeed = it
            },
            onMove = { fromIndex, toIndex ->
                persons.moveItem(fromIndex, toIndex)
            },
            onMoveFinished = onSave,
            onDelete = { person ->
                persons.remove(person)

                val keysToRemove = checkedMap.keys.filter {
                    checkedKeyPerson(it) == person
                }
                val disabledKeysToRemove = disabledRoleMap.keys.filter {
                    checkedKeyPerson(it) == person
                }

                keysToRemove.forEach {
                    checkedMap.remove(it)
                }
                disabledKeysToRemove.forEach {
                    disabledRoleMap.remove(it)
                }

                if (expandedPersonName == person) {
                    expandedPersonName = null
                }

                onSave()
            },
            expandedContent = { person ->
                val allEnabled = tasks.all { task ->
                    disabledRoleMap[roleEnabledKey(person, task.name)] != true
                }
                val onToggleAll = {
                    if (allEnabled) {
                        tasks.forEach { task ->
                            val key = roleEnabledKey(person, task.name)
                            disabledRoleMap[key] = true
                            checkedMap.remove(key)
                        }
                    } else {
                        tasks.forEach { task ->
                            disabledRoleMap.remove(roleEnabledKey(person, task.name))
                        }
                    }

                    onSave()
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        tasks.forEachIndexed { index, task ->
                            val key = roleEnabledKey(person, task.name)
                            val enabled = disabledRoleMap[key] != true
                            val rowModifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (index < 2) {
                                        Modifier.padding(end = 112.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                                .then(
                                    if (tasks.size == 1) {
                                        Modifier.heightIn(min = 54.dp)
                                    } else {
                                        Modifier
                                    }
                                )

                            EnableRoleRow(
                                person = "${task.name} · ${resetText(task)}",
                                enabled = enabled,
                                modifier = rowModifier,
                                onClick = {
                                    if (enabled) {
                                        disabledRoleMap[key] = true
                                        checkedMap.remove(key)
                                    } else {
                                        disabledRoleMap.remove(key)
                                    }

                                    onSave()
                                }
                            )
                        }
                    }

                    ToggleAllRolesButton(
                        text = if (allEnabled) "一键关闭" else "一键启用",
                        onClick = onToggleAll,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "管理游戏内容",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "可展开列表给角色分配任务",
            fontSize = 14.sp,
            color = Color(0xFF777777)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AddRow(
            value = newTask,
            label = "输入游戏内容名称",
            onValueChange = { newTask = it },
            onAdd = {
                val taskName = newTask.trim()

                if (
                    taskName.isNotBlank() &&
                    !containsReservedSeparator(taskName) &&
                    tasks.none { it.name == taskName }
                ) {
                    tasks.add(
                        TaskItem(
                            name = taskName,
                            resetType = newTaskResetType,
                            resetDay = newTaskResetDay
                        )
                    )

                    newTask = ""
                    onSave()
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            ChoiceButton(
                text = "每日重置",
                selected = newTaskResetType == RESET_TYPE_DAILY,
                onClick = {
                    newTaskResetType = RESET_TYPE_DAILY
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            ChoiceButton(
                text = "每周重置",
                selected = newTaskResetType == RESET_TYPE_WEEKLY,
                onClick = {
                    newTaskResetType = RESET_TYPE_WEEKLY
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            ChoiceButton(
                text = "一次性",
                selected = newTaskResetType == RESET_TYPE_ONCE,
                onClick = {
                    newTaskResetType = RESET_TYPE_ONCE
                }
            )
        }

        if (newTaskResetType == RESET_TYPE_WEEKLY) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 1..4) {
                    WeekDayButton(
                        day = i,
                        selectedDay = newTaskResetDay,
                        onClick = {
                            newTaskResetDay = i
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 5..7) {
                    WeekDayButton(
                        day = i,
                        selectedDay = newTaskResetDay,
                        onClick = {
                            newTaskResetDay = i
                        }
                    )
                }
            }
        }

        ReorderableDeleteList(
            items = tasks,
            itemKey = { it.name },
            itemText = { "${it.name} · ${resetText(it)}" },
            canExpand = { persons.isNotEmpty() },
            isExpanded = { persons.isNotEmpty() && expandedTaskName == it.name },
            onRowClick = { task ->
                if (persons.isNotEmpty()) {
                    expandedTaskName = if (expandedTaskName == task.name) {
                        null
                    } else {
                        task.name
                    }
                }
            },
            onDragStateChange = {
                isReordering = it
                if (!it) {
                    autoScrollSpeed = 0f
                }
            },
            screenHeightPx = screenHeightPx,
            edgeScrollAreaPx = edgeScrollAreaPx,
            onAutoScrollSpeedChange = {
                autoScrollSpeed = it
            },
            onMove = { fromIndex, toIndex ->
                tasks.moveItem(fromIndex, toIndex)
            },
            onMoveFinished = onSave,
            onDelete = { task ->
                tasks.remove(task)

                val keysToRemove = checkedMap.keys.filter {
                    checkedKeyTaskName(it) == task.name
                }
                val disabledKeysToRemove = disabledRoleMap.keys.filter {
                    checkedKeyTaskName(it) == task.name
                }

                keysToRemove.forEach {
                    checkedMap.remove(it)
                }
                disabledKeysToRemove.forEach {
                    disabledRoleMap.remove(it)
                }

                if (expandedTaskName == task.name) {
                    expandedTaskName = null
                }

                onSave()
            },
            expandedContent = { task ->
                val allEnabled = persons.all { person ->
                    disabledRoleMap[roleEnabledKey(person, task.name)] != true
                }
                val onToggleAll = {
                    if (allEnabled) {
                        persons.forEach { person ->
                            val key = roleEnabledKey(person, task.name)
                            disabledRoleMap[key] = true
                            checkedMap.remove(key)
                        }
                    } else {
                        persons.forEach { person ->
                            disabledRoleMap.remove(roleEnabledKey(person, task.name))
                        }
                    }

                    onSave()
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        persons.forEachIndexed { index, person ->
                            val key = roleEnabledKey(person, task.name)
                            val enabled = disabledRoleMap[key] != true
                            val rowModifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (index < 2) {
                                        Modifier.padding(end = 112.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                                .then(
                                    if (persons.size == 1) {
                                        Modifier.heightIn(min = 54.dp)
                                    } else {
                                        Modifier
                                    }
                                )

                            EnableRoleRow(
                                person = person,
                                enabled = enabled,
                                modifier = rowModifier,
                                onClick = {
                                    if (enabled) {
                                        disabledRoleMap[key] = true
                                        checkedMap.remove(key)
                                    } else {
                                        disabledRoleMap.remove(key)
                                    }

                                    onSave()
                                }
                            )
                        }
                    }

                    ToggleAllRolesButton(
                        text = if (allEnabled) "一键关闭" else "一键启用",
                        onClick = onToggleAll,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        )
    }
}

@Composable
fun ChoiceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                Color(0xFF4CAF50)
            } else {
                Color(0xFF9E9E9E)
            }
        )
    ) {
        Text(
            text = if (selected) "✓ $text" else text,
            color = Color.White
        )
    }
}

@Composable
fun WeekDayButton(
    day: Int,
    selectedDay: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedDay == day) {
                Color(0xFF4CAF50)
            } else {
                Color(0xFF9E9E9E)
            }
        )
    ) {
        Text(
            text = if (selectedDay == day) {
                "✓ 周${weekDayName(day)}"
            } else {
                "周${weekDayName(day)}"
            },
            color = Color.White
        )
    }
}

@Composable
fun TopTitle(
    title: String,
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = onBack) {
            Text("返回")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )
    }
}

@Composable
fun TopTitleWithReset(
    title: String,
    onBack: () -> Unit,
    onResetAll: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        ConfirmDialog(
            title = "确认重置",
            message = "确定要重置全部完成记录吗？这个操作不能撤销。",
            confirmText = "确认重置",
            onConfirm = {
                showResetConfirm = false
                onResetAll()
            },
            onDismiss = {
                showResetConfirm = false
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = onBack) {
            Text("返回")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            color = Color(0xFF222222)
        )

        Button(onClick = { showResetConfirm = true }) {
            Text("全部重置")
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ProgressCard(
    title: String,
    progress: String,
    completed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (completed)
                    Color(0xFFDFF5E3)
                else
                    Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color =
                    if (completed)
                        Color(0xFF2E7D32)
                    else
                        Color(0xFF333333)
            )

            Text(
                text = progress,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A5ACD)
            )
        }
    }
}

@Composable
fun CheckRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 8.dp)
            .clickable {
                onCheckedChange(!checked)
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) {
                Color(0xFFDFF5E3)
            } else {
                Color.White
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                color = Color(0xFF333333),
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.weight(1f))

            if (checked) {
                Text(
                    text = "已完成",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddRow(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text("新增")
        }
    }
}

@Composable
fun <T> ReorderableDeleteList(
    items: List<T>,
    itemKey: (T) -> String,
    itemText: (T) -> String,
    canExpand: (T) -> Boolean = { false },
    isExpanded: (T) -> Boolean = { false },
    onRowClick: ((T) -> Unit)? = null,
    onDragStateChange: (Boolean) -> Unit,
    screenHeightPx: Float,
    edgeScrollAreaPx: Float,
    onAutoScrollSpeedChange: (Float) -> Unit,
    onMove: (Int, Int) -> Unit,
    onMoveFinished: () -> Unit,
    onDelete: (T) -> Unit,
    expandedContent: @Composable (T) -> Unit = {}
) {
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val moveThreshold = with(LocalDensity.current) { 64.dp.toPx() }

    items.forEach { item ->
        val key = itemKey(item)

        key(key) {
            var handleTopInWindow by remember { mutableFloatStateOf(0f) }
            val isDragging = draggingKey == key
            val expandable = canExpand(item)

            DeleteRow(
                text = itemText(item),
                isDragging = isDragging,
                isExpanded = isExpanded(item),
                showExpandIcon = expandable && onRowClick != null,
                dragOffset = if (isDragging) dragOffset else 0f,
                dragHandleModifier = Modifier
                    .onGloballyPositioned {
                        handleTopInWindow = it.positionInWindow().y
                    }
                    .pointerInput(key, items.size) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            draggingKey = key
                            dragOffset = 0f
                            onAutoScrollSpeedChange(0f)
                            onDragStateChange(true)
                        },
                        onDragEnd = {
                            draggingKey = null
                            dragOffset = 0f
                            onAutoScrollSpeedChange(0f)
                            onDragStateChange(false)
                            onMoveFinished()
                        },
                        onDragCancel = {
                            draggingKey = null
                            dragOffset = 0f
                            onAutoScrollSpeedChange(0f)
                            onDragStateChange(false)
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()

                            val currentKey = draggingKey ?: return@detectVerticalDragGestures
                            val currentIndex = items.indexOfFirst { itemKey(it) == currentKey }
                            if (currentIndex == -1) {
                                return@detectVerticalDragGestures
                            }

                            dragOffset += dragAmount
                            val pointerY = handleTopInWindow + change.position.y
                            val scrollSpeed = when {
                                pointerY < edgeScrollAreaPx ->
                                    -((edgeScrollAreaPx - pointerY) / edgeScrollAreaPx * 18f)

                                pointerY > screenHeightPx - edgeScrollAreaPx ->
                                    ((pointerY - (screenHeightPx - edgeScrollAreaPx)) / edgeScrollAreaPx * 18f)

                                else -> 0f
                            }.coerceIn(-18f, 18f)

                            onAutoScrollSpeedChange(scrollSpeed)

                            if (dragOffset > moveThreshold && currentIndex < items.lastIndex) {
                                onMove(currentIndex, currentIndex + 1)
                                dragOffset -= moveThreshold
                            } else if (dragOffset < -moveThreshold && currentIndex > 0) {
                                onMove(currentIndex, currentIndex - 1)
                                dragOffset += moveThreshold
                            }
                        }
                    )
                },
                onRowClick = if (onRowClick == null || !expandable) {
                    null
                } else {
                    { onRowClick(item) }
                },
                onDelete = {
                    onDelete(item)
                },
                expandedContent = {
                    expandedContent(item)
                }
            )
        }
    }
}

@Suppress("ModifierParameter")
@Composable
fun DeleteRow(
    text: String,
    isDragging: Boolean = false,
    isExpanded: Boolean = false,
    showExpandIcon: Boolean = false,
    dragOffset: Float = 0f,
    dragHandleModifier: Modifier = Modifier,
    onRowClick: (() -> Unit)? = null,
    onDelete: () -> Unit,
    expandedContent: @Composable () -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val rowScale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        label = "deleteRowScale"
    )
    val rowElevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        label = "deleteRowElevation"
    )

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "确认删除",
            message = "确定要删除“$text”吗？相关完成记录也会一起删除。",
            confirmText = "确认删除",
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = {
                showDeleteConfirm = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset {
                IntOffset(0, dragOffset.roundToInt())
            }
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                scaleX = rowScale
                scaleY = rowScale
            }
            .shadow(
                elevation = rowElevation,
                shape = RoundedCornerShape(14.dp),
                clip = false
            )
            .padding(top = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                Color(0xFFE8F0FE)
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = rowElevation
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "≡",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF777777),
                    modifier = dragHandleModifier
                        .width(34.dp)
                        .padding(end = 8.dp)
                )

                Text(
                    text = text,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (onRowClick == null) {
                                Modifier
                            } else {
                                Modifier.clickable { onRowClick() }
                            }
                        )
                        .padding(vertical = 8.dp)
                )

                if (showExpandIcon) {
                    ExpandStateIcon(
                        expanded = isExpanded,
                        onClick = {
                            onRowClick?.invoke()
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(onClick = { showDeleteConfirm = true }) {
                    Text("删除")
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 14.dp, bottom = 10.dp)
                ) {
                    expandedContent()
                }
            }
        }
    }
}

@Composable
fun ExpandStateIcon(
    expanded: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val strokeWidth = 2.dp.toPx()
            val color = Color(0xFFC4C8CE)
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val leg = size.minDimension * 0.42f
            val left = Offset(centerX - leg, centerY)
            val right = Offset(centerX + leg, centerY)
            val tip = Offset(centerX, centerY + if (expanded) -leg else leg)

            if (expanded) {
                drawLine(
                    color = color,
                    start = tip,
                    end = left,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = tip,
                    end = right,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            } else {
                drawLine(
                    color = color,
                    start = left,
                    end = tip,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = right,
                    end = tip,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
@Composable
fun ToggleAllRolesButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .width(104.dp)
            .height(54.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color(0xFFEDEFF3),
            contentColor = Color(0xFF333333)
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun EnableRoleRow(
    person: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFF9E9E9E),
                    shape = CircleShape
                )
                .background(
                    color = if (enabled) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFD0D0D0)
                    },
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = person,
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )
    }
}

