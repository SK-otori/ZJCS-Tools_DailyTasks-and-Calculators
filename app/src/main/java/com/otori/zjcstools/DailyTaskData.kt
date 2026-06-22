package com.otori.zjcstools

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDate
import java.time.LocalTime
data class TaskItem(
    val name: String,
    val resetType: String,
    val resetDay: Int = 1
)

private const val LIST_SEPARATOR = "###"
private const val TASK_FIELD_SEPARATOR = "@@"
private const val CHECKED_KEY_SEPARATOR = "|"
private const val RESET_HOUR = 8
const val RESET_TYPE_DAILY = "DAILY"
const val RESET_TYPE_WEEKLY = "WEEKLY"
const val RESET_TYPE_ONCE = "ONCE"
private const val DEFAULT_DAILY_DATA_VERSION = 2


private val defaultTasks = listOf(
    TaskItem("每日副本", RESET_TYPE_DAILY),
    TaskItem("圣兽讨伐", RESET_TYPE_DAILY),
    TaskItem("双影幻境", RESET_TYPE_DAILY),
    TaskItem("每日任务扫尾", RESET_TYPE_DAILY),
    TaskItem("大矿", RESET_TYPE_WEEKLY, 1),
    TaskItem("竞技场", RESET_TYPE_WEEKLY, 1),
    TaskItem("每周轮换活动", RESET_TYPE_WEEKLY, 2)
)

private val legacyDefaultTasks = listOf(
    TaskItem("每日副本", RESET_TYPE_DAILY),
    TaskItem("圣兽讨伐", RESET_TYPE_DAILY),
    TaskItem("双影幻境", RESET_TYPE_DAILY),
    TaskItem("秘宝大作战", RESET_TYPE_DAILY),
    TaskItem("每周轮换活动", RESET_TYPE_WEEKLY, 2),
    TaskItem("竞技场", RESET_TYPE_WEEKLY, 1)
)

private val defaultPersons = listOf(
    "源羽°",
    "神明丶",
    "超幸运炫彩米",
    "紫衣°",
    "Alisa",
    "秋冬",
    "残阳暮雪",
    "葡萄D",
    "小源羽"
)

private val legacyDefaultPersons = listOf(
    "示例角色1",
    "示例角色2",
    "示例角色3",
    "示例角色4"
)

fun weekDayName(day: Int): String {
    return when (day) {
        1 -> "一"
        2 -> "二"
        3 -> "三"
        4 -> "四"
        5 -> "五"
        6 -> "六"
        else -> "日"
    }
}

fun resetText(task: TaskItem): String {
    return when (task.resetType) {
        RESET_TYPE_WEEKLY -> "每周${weekDayName(task.resetDay)}重置"
        RESET_TYPE_ONCE -> "一次性"
        else -> "每日重置"
    }
}

fun taskToText(task: TaskItem): String {
    return "${task.name}$TASK_FIELD_SEPARATOR${task.resetType}$TASK_FIELD_SEPARATOR${task.resetDay}"
}

fun tasksToText(tasks: List<TaskItem>): String {
    return tasks.joinToString(LIST_SEPARATOR) {
        taskToText(it)
    }
}

fun personsToText(persons: List<String>): String {
    return persons.joinToString(LIST_SEPARATOR)
}

fun textToTask(text: String): TaskItem {
    val parts = text.split(TASK_FIELD_SEPARATOR)

    return if (parts.size >= 3) {
        TaskItem(
            name = parts[0],
            resetType = parts[1],
            resetDay = parts[2].toIntOrNull() ?: 1
        )
    } else {
        TaskItem(
            name = text,
            resetType = RESET_TYPE_DAILY,
            resetDay = 1
        )
    }
}

fun checkedKey(person: String, taskName: String): String {
    return "$person$CHECKED_KEY_SEPARATOR$taskName"
}

fun checkedKeyPerson(key: String): String {
    return key.substringBefore(CHECKED_KEY_SEPARATOR, missingDelimiterValue = "")
}

fun checkedKeyTaskName(key: String): String {
    return key.substringAfter(CHECKED_KEY_SEPARATOR, missingDelimiterValue = "")
}

fun roleEnabledKey(person: String, taskName: String): String {
    return checkedKey(person, taskName)
}

fun isRoleEnabled(
    disabledRoleMap: Map<String, Boolean>,
    person: String,
    taskName: String
): Boolean {
    return disabledRoleMap[roleEnabledKey(person, taskName)] != true
}

fun containsReservedSeparator(text: String): Boolean {
    return text.contains(LIST_SEPARATOR) ||
            text.contains(TASK_FIELD_SEPARATOR) ||
            text.contains(CHECKED_KEY_SEPARATOR)
}

fun <T> MutableList<T>.moveItem(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) {
        return
    }

    val item = removeAt(fromIndex)
    add(toIndex, item)
}

fun saveData(
    context: Context,
    tasks: List<TaskItem>,
    persons: List<String>,
    checkedMap: Map<String, Boolean>,
    disabledRoleMap: Map<String, Boolean>
) {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    val checkedKeys = checkedMap
        .filter { it.value }
        .keys
        .joinToString(LIST_SEPARATOR)

    val disabledRoleKeys = disabledRoleMap
        .filter { it.value }
        .keys
        .joinToString(LIST_SEPARATOR)

    prefs.edit(commit = true) {
        putString("tasks", tasksToText(tasks))
        putString("persons", personsToText(persons))
        putString("checked", checkedKeys)
        putString("disabled_roles", disabledRoleKeys)
    }
}

fun migrateDefaultDailyDataIfNeeded(context: Context) {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    if (prefs.getInt("default_daily_data_version", 0) >= DEFAULT_DAILY_DATA_VERSION) {
        return
    }

    val taskText = prefs.getString("tasks", null)
    val personText = prefs.getString("persons", null)
    val legacyTaskText = tasksToText(legacyDefaultTasks)
    val legacyPersonText = personsToText(legacyDefaultPersons)

    prefs.edit(commit = true) {
        if (taskText.isNullOrBlank() || taskText == legacyTaskText) {
            putString("tasks", tasksToText(defaultTasks))
        }

        if (personText.isNullOrBlank() || personText == legacyPersonText) {
            putString("persons", personsToText(defaultPersons))
        }

        putInt("default_daily_data_version", DEFAULT_DAILY_DATA_VERSION)
    }
}

fun loadTasks(context: Context): List<TaskItem> {
    migrateDefaultDailyDataIfNeeded(context)

    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val text = prefs.getString("tasks", null)

    return if (text.isNullOrBlank()) {
        defaultTasks
    } else {
        text.split(LIST_SEPARATOR).map {
            textToTask(it)
        }
    }
}

fun loadPersons(context: Context): List<String> {
    migrateDefaultDailyDataIfNeeded(context)

    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val text = prefs.getString("persons", null)

    return if (text.isNullOrBlank()) {
        defaultPersons
    } else {
        text.split(LIST_SEPARATOR)
    }
}

fun loadCheckedMap(context: Context): MutableMap<String, Boolean> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val text = prefs.getString("checked", "")

    val map = mutableMapOf<String, Boolean>()

    if (!text.isNullOrBlank()) {
        text.split(LIST_SEPARATOR).forEach {
            map[it] = true
        }
    }

    return map
}

fun loadDisabledRoleMap(context: Context): MutableMap<String, Boolean> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val text = prefs.getString("disabled_roles", "")

    val map = mutableMapOf<String, Boolean>()

    if (!text.isNullOrBlank()) {
        text.split(LIST_SEPARATOR).forEach {
            map[it] = true
        }
    }

    return map
}

fun weeklyResetDayPassed(lastDate: LocalDate, today: LocalDate, resetDay: Int): Boolean {
    var day = lastDate.plusDays(1)

    while (!day.isAfter(today)) {
        if (day.dayOfWeek.value == resetDay) {
            return true
        }

        day = day.plusDays(1)
    }

    return false
}

fun resetDateFor(date: LocalDate, time: LocalTime): LocalDate {
    return if (time.hour < RESET_HOUR) {
        date.minusDays(1)
    } else {
        date
    }
}

fun resetIfNeeded(
    context: Context,
    tasks: MutableList<TaskItem>,
    checkedMap: MutableMap<String, Boolean>,
    disabledRoleMap: MutableMap<String, Boolean>
) {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    val today = resetDateFor(LocalDate.now(), LocalTime.now())
    val todayText = today.toString()
    val lastDateText = prefs.getString("last_date", null)

    if (lastDateText == null) {
        prefs.edit(commit = true) {
            putString("last_date", todayText)
        }
        return
    }

    if (lastDateText == todayText) {
        return
    }

    val lastDate = try {
        LocalDate.parse(lastDateText)
    } catch (_: Exception) {
        today.minusDays(1)
    }

    val taskNamesToReset = tasks.filter { task ->
        task.resetType == RESET_TYPE_DAILY ||
                (task.resetType == RESET_TYPE_WEEKLY && weeklyResetDayPassed(lastDate, today, task.resetDay))
    }.map {
        it.name
    }

    val onceTaskNames = tasks
        .filter { it.resetType == RESET_TYPE_ONCE }
        .map { it.name }

    val keysToRemove = checkedMap.keys.filter { key ->
        (taskNamesToReset + onceTaskNames).any { taskName ->
            checkedKeyTaskName(key) == taskName
        }
    }

    keysToRemove.forEach { key ->
        if (checkedKeyTaskName(key) in onceTaskNames) {
            disabledRoleMap[key] = true
        }
    }

    keysToRemove.forEach {
        checkedMap.remove(it)
    }

    prefs.edit(commit = true) {
        putString("last_date", todayText)
    }
}

