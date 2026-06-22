package com.otori.zjcstools

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}



@Composable
fun App() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentMode by remember { mutableStateOf("home") }
    val navigationStack = remember { mutableStateListOf<String>() }
    var forwardNavigation by remember { mutableStateOf(true) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var updatePreviewNoticeList by remember {
        mutableStateOf(loadCachedUpdatePreviewNotices(context))
    }
    var exchangeCodeNoticeList by remember {
        mutableStateOf(loadCachedExchangeCodeNotices(context))
    }
    var updatePreviewErrorText by remember { mutableStateOf<String?>(null) }
    var exchangeCodeErrorText by remember { mutableStateOf<String?>(null) }
    var selectedUpdatePreviewNoticeId by remember {
        mutableStateOf(updatePreviewNoticeList.maxByOrNull { it.date }?.id.orEmpty())
    }

    val bgList = listOf(
        R.drawable.zjcs_bg1,
        R.drawable.zjcs_bg2,
        R.drawable.zjcs_bg3,
        R.drawable.zjcs_bg4,
        R.drawable.zjcs_bg5,
        R.drawable.zjcs_bg6,
        R.drawable.zjcs_bg7
    )

    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    var visibleExchangeCodeNotices by remember {
        mutableStateOf(activeExchangeCodeNotices(context, LocalDate.now(), exchangeCodeNoticeList))
    }

    val bgIndex = remember {
        val lastIndex = prefs.getInt("last_bg", -1)

        var newIndex: Int

        do {
            newIndex = Random.nextInt(bgList.size)
        } while (newIndex == lastIndex)

        prefs.edit(commit = true) {
            putInt("last_bg", newIndex)
        }

        newIndex
    }

    val currentBg = bgList[bgIndex]
    val backgroundTransition = rememberInfiniteTransition(label = "backgroundPanorama")
    val backgroundProgress by backgroundTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 45000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backgroundProgress"
    )

    val tasks = remember {
        mutableStateListOf<TaskItem>().apply {
            addAll(loadTasks(context))
        }
    }

    val persons = remember {
        mutableStateListOf<String>().apply {
            addAll(loadPersons(context))
        }
    }

    val checkedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            putAll(loadCheckedMap(context))
        }
    }

    val disabledRoleMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            putAll(loadDisabledRoleMap(context))
        }
    }

    fun saveNow() {
        saveData(context, tasks, persons, checkedMap, disabledRoleMap)
    }

    fun refreshResetAndSave() {
        resetIfNeeded(context, tasks, checkedMap, disabledRoleMap)
        saveNow()
    }

    fun navigateTo(mode: String) {
        if (currentMode == mode) {
            return
        }

        navigationStack.add(currentMode)
        forwardNavigation = true
        currentMode = mode
    }

    fun goBack(): Boolean {
        val previousMode = navigationStack.removeLastOrNull() ?: return false
        forwardNavigation = false
        currentMode = previousMode
        return true
    }

    LaunchedEffect(Unit) {
        refreshResetAndSave()

        updatePreviewNoticeList = loadRemoteUpdatePreviewNotices(context)
        if (updatePreviewNoticeList.none { it.id == selectedUpdatePreviewNoticeId }) {
            selectedUpdatePreviewNoticeId = updatePreviewNoticeList.maxByOrNull { it.date }?.id.orEmpty()
        }

        exchangeCodeNoticeList = loadRemoteExchangeCodeNotices(context)
        visibleExchangeCodeNotices = activeExchangeCodeNotices(
            context = context,
            today = LocalDate.now(),
            notices = exchangeCodeNoticeList
        )
    }

    LaunchedEffect(currentMode) {
        when (currentMode) {
            "exchange_codes" -> {
                exchangeCodeErrorText = null

                fetchRemoteExchangeCodeNoticesStrict(context)
                    .onSuccess { notices ->
                        exchangeCodeNoticeList = notices
                    }
                    .onFailure { error ->
                        exchangeCodeErrorText = if (
                            error is RemoteFileUnavailableException ||
                            error !is IOException
                        ) {
                            "远程文件不可用"
                        } else {
                            "网络异常"
                        }
                    }
            }

            "update_preview_home" -> {
                updatePreviewErrorText = null

                fetchRemoteUpdatePreviewNoticesStrict(context)
                    .onSuccess { notices ->
                        updatePreviewNoticeList = notices
                        if (updatePreviewNoticeList.none { it.id == selectedUpdatePreviewNoticeId }) {
                            selectedUpdatePreviewNoticeId = updatePreviewNoticeList
                                .maxByOrNull { it.date }
                                ?.id
                                .orEmpty()
                        }
                    }
                    .onFailure { error ->
                        updatePreviewErrorText = if (
                            error is RemoteFileUnavailableException ||
                            error !is IOException
                        ) {
                            "远程文件不可用"
                        } else {
                            "网络异常"
                        }
                    }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> refreshResetAndSave()
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> saveNow()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler {
        if (goBack()) {
            return@BackHandler
        }

        val now = System.currentTimeMillis()

        if (now - lastBackPressTime <= 2000L) {
            saveNow()
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = now
            Toast.makeText(context, "再按一次退出", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        PanoramaBackground(
            backgroundRes = currentBg,
            progress = backgroundProgress
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
        )

        AnimatedContent(
            targetState = currentMode,
            transitionSpec = {
                val initialScale = if (forwardNavigation) {
                    0.92f
                } else {
                    1.06f
                }

                (scaleIn(
                    initialScale = initialScale,
                    animationSpec = tween(durationMillis = 160)
                ) + fadeIn(animationSpec = tween(durationMillis = 100)) togetherWith ExitTransition.None)
                    .apply {
                        targetContentZIndex = 1f
                    }
            },
            label = "screenTransition"
        ) { mode ->
            when (mode) {
            "home" -> MainHomeScreen(
                onDailyRecordClick = { navigateTo("daily_home") },
                onToolsClick = { navigateTo("tools_home") },
                onGameDataClick = { navigateTo("game_data_home") },
                onExchangeCodesClick = { navigateTo("exchange_codes") },
                onUpdatePreviewClick = { navigateTo("update_preview_home") }
            )

            "daily_home" -> HomeScreen(
                onTaskModeClick = { navigateTo("task") },
                onPersonModeClick = { navigateTo("person") },
                onManageClick = { navigateTo("manage") },
                onBack = { goBack() }
            )

            "tools_home" -> ToolsHomeScreen(
                onDungeonCalculatorClick = { navigateTo("tools_dungeon_morning_star") },
                onAttributeCalculatorClick = { navigateTo("tools_attribute_morning_star") },
                onAstralKamiCalculatorClick = { navigateTo("tools_astral_kami") },
                onAssaultArmorBreakCalculatorClick = { navigateTo("tools_assault_armor_break") },
                onBack = { goBack() }
            )

            "tools_dungeon_morning_star" -> DungeonMorningStarScreen(
                onBack = { goBack() }
            )

            "tools_attribute_morning_star" -> AttributeMorningStarScreen(
                onBack = { goBack() }
            )

            "tools_astral_kami" -> AstralKamiScreen(
                onBack = { goBack() }
            )

            "tools_assault_armor_break" -> AssaultArmorBreakScreen(
                onBack = { goBack() }
            )

            "game_data_home" -> GameDataHomeScreen(
                onBack = { goBack() }
            )

            "exchange_codes" -> ExchangeCodeScreen(
                notices = exchangeCodeNoticeList,
                errorText = exchangeCodeErrorText,
                onShowLocalData = {
                    exchangeCodeErrorText = null
                },
                onCopy = { code ->
                    copyExchangeCode(context, code)
                },
                onBack = { goBack() }
            )

            "update_preview_home" -> UpdatePreviewHomeScreen(
                notices = updatePreviewNoticeList,
                errorText = updatePreviewErrorText,
                onShowLocalData = {
                    updatePreviewErrorText = null
                },
                onNoticeClick = { noticeId ->
                    selectedUpdatePreviewNoticeId = noticeId
                    navigateTo("update_preview_detail")
                },
                onBack = { goBack() }
            )

            "update_preview_detail" -> {
                val selectedNotice = updatePreviewNoticeList
                    .firstOrNull { it.id == selectedUpdatePreviewNoticeId }
                    ?: updatePreviewNoticeList.maxByOrNull { it.date }

                if (selectedNotice != null) {
                    UpdatePreviewDetailScreen(
                        notice = selectedNotice,
                        onBack = { goBack() }
                    )
                } else {
                    SecondaryHomeScreen(
                        title = "更新公告",
                        subtitle = "暂无公告",
                        onBack = { goBack() }
                    )
                }
            }

            "task" -> TaskModeScreen(
                tasks = tasks,
                persons = persons,
                checkedMap = checkedMap,
                disabledRoleMap = disabledRoleMap,
                onCheckedChange = { key, value ->
                    checkedMap[key] = value
                    saveNow()
                },
                onResetAll = {
                    checkedMap.clear()
                    saveNow()
                },
                onBack = { goBack() }
            )

            "person" -> PersonModeScreen(
                tasks = tasks,
                persons = persons,
                checkedMap = checkedMap,
                disabledRoleMap = disabledRoleMap,
                onCheckedChange = { key, value ->
                    checkedMap[key] = value
                    saveNow()
                },
                onResetAll = {
                    checkedMap.clear()
                    saveNow()
                },
                onBack = { goBack() }
            )

            "manage" -> ManageScreen(
                tasks = tasks,
                persons = persons,
                checkedMap = checkedMap,
                disabledRoleMap = disabledRoleMap,
                onSave = { saveNow() },
                onBack = { goBack() }
            )
        }
    }

        if (visibleExchangeCodeNotices.isNotEmpty()) {
            ExchangeCodeNoticeDialog(
                notices = visibleExchangeCodeNotices,
                onCopy = { code ->
                    copyExchangeCode(context, code)
                },
                onNeverRemind = {
                    hideExchangeCodeNotices(context, visibleExchangeCodeNotices)
                    visibleExchangeCodeNotices = emptyList()
                },
                onDismiss = {
                    visibleExchangeCodeNotices = emptyList()
                }
            )
        }
}
}


@Composable
fun MainHomeScreen(
    onDailyRecordClick: () -> Unit,
    onToolsClick: () -> Unit,
    onGameDataClick: () -> Unit,
    onExchangeCodesClick: () -> Unit,
    onUpdatePreviewClick: () -> Unit
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
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            StrokeText(
                text = "杖剑工具",
                fontSize = 40,
                fillColor = Color.White,
                strokeColor = Color(0xFF202020),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            StrokeText(
                text = "制作者：陨落峡谷-音璃",
                fontSize = 24,
                fillColor = Color.White,
                strokeColor = Color(0xFF202020)
            )

            Spacer(modifier = Modifier.height(60.dp))

            HomeCardButton(
                title = "日常记录",
                subtitle = "记录游戏内容、角色完成情况和任务分配",
                onClick = onDailyRecordClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            HomeCardButton(
                title = "小工具",
                subtitle = "放置计算器和实用辅助工具",
                onClick = onToolsClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            HomeCardButton(
                title = "游戏数据",
                subtitle = "整理和查看游戏相关数据",
                onClick = onGameDataClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            HomeCardButton(
                title = EXCHANGE_CODE_CARD_TITLE,
                subtitle = EXCHANGE_CODE_CARD_SUBTITLE,
                onClick = onExchangeCodesClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            HomeCardButton(
                title = UPDATE_PREVIEW_CARD_TITLE,
                subtitle = UPDATE_PREVIEW_CARD_SUBTITLE,
                onClick = onUpdatePreviewClick
            )
        }
    }
}


@Composable
fun ToolsHomeScreen(
    onDungeonCalculatorClick: () -> Unit,
    onAttributeCalculatorClick: () -> Unit,
    onAstralKamiCalculatorClick: () -> Unit,
    onAssaultArmorBreakCalculatorClick: () -> Unit,
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "小工具",
        subtitle = "放置计算器和实用辅助工具",
        onBack = onBack
    ) {
        HomeCardButton(
            title = "副本晨星计算器",
            subtitle = "计算新本噩梦、新本炼狱和君临深渊的晨星期望",
            onClick = onDungeonCalculatorClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        HomeCardButton(
            title = "属性晨星性价比",
            subtitle = "施工中",
            onClick = onAttributeCalculatorClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        HomeCardButton(
            title = "星间之神好感度",
            subtitle = "计算星间之神最终等级",
            onClick = onAstralKamiCalculatorClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        HomeCardButton(
            title = "强袭破甲增伤计算器",
            subtitle = "计算强袭破甲增伤收益",
            onClick = onAssaultArmorBreakCalculatorClick
        )
    }
}


@Composable
fun GameDataHomeScreen(
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "游戏数据",
        subtitle = "整理和查看游戏相关数据",
        onBack = onBack
    )
}


@Composable
fun SecondaryHomeScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    pinnedTitleBar: Boolean = false,
    content: @Composable () -> Unit = {}
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
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            StrokeText(
                text = title,
                fontSize = 40,
                fillColor = Color.White,
                strokeColor = Color(0xFF202020),
                fontWeight = FontWeight.Bold
            )

            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                StrokeText(
                    text = subtitle,
                    fontSize = 24,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF202020)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            content()
        }

        if (pinnedTitleBar) {
            PinnedToolTitleBar(
                title = title,
                onBack = onBack
            )
        } else {
            TopBackButton(onBack = onBack)
        }
    }
/*
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF6F7FB))
            .safeDrawingPadding()
            .padding(20.dp)
    ) {
        TopTitle(title = "小工具", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Text(
                text = "等待导入 Excel 计算器",
                fontSize = 18.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(18.dp)
            )
        }
    }
*/
}

@Composable
fun PinnedToolTitleBar(
    title: String,
    onBack: () -> Unit
) {
    val backInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDEDED))
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(100.dp)
                .height(31.dp)
                .clickable(
                    interactionSource = backInteractionSource,
                    indication = null,
                    onClick = onBack
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "< 返回",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F3A8F)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 86.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                color = Color(0xFF222222),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TopBackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Button(onClick = onBack) {
            Text("返回")
        }
    }
}

@Composable
@Suppress("LocalContextResourcesRead")
fun PanoramaBackground(
    backgroundRes: Int,
    progress: Float
) {
    val context = LocalContext.current

    val bitmap = remember(backgroundRes) {
        BitmapFactory.decodeResource(context.resources, backgroundRes)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val screenRatio = size.width / size.height
        val imageRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        if (imageRatio > screenRatio) {
            val srcWidth = (bitmap.height * screenRatio).toInt()

            val maxLeft = bitmap.width - srcWidth
            val left = (maxLeft * progress).toInt()

            drawIntoCanvas {
                it.nativeCanvas.drawBitmap(
                    bitmap,
                    Rect(
                        left,
                        0,
                        left + srcWidth,
                        bitmap.height
                    ),
                    RectF(
                        0f,
                        0f,
                        size.width,
                        size.height
                    ),
                    null
                )
            }
        } else {
            val srcHeight = (bitmap.width / screenRatio).toInt()

            val maxTop = bitmap.height - srcHeight
            val top = (maxTop * progress).toInt()

            drawIntoCanvas {
                it.nativeCanvas.drawBitmap(
                    bitmap,
                    Rect(
                        0,
                        top,
                        bitmap.width,
                        top + srcHeight
                    ),
                    RectF(
                        0f,
                        0f,
                        size.width,
                        size.height
                    ),
                    null
                )
            }
        }
    }
}

@Composable
fun HomeCardButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.zjcs_icon_star),
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}


@Composable
fun StrokeText(
    text: String,
    fontSize: Int,
    fillColor: Color,
    strokeColor: Color,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Box {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = strokeColor,
            modifier = Modifier.offset((-1).dp, 0.dp)
        )

        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = strokeColor,
            modifier = Modifier.offset((1).dp, 0.dp)
        )

        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = strokeColor,
            modifier = Modifier.offset(0.dp, (-1).dp)
        )

        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = strokeColor,
            modifier = Modifier.offset(0.dp, (1).dp)
        )

        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = fillColor
        )
    }
}



