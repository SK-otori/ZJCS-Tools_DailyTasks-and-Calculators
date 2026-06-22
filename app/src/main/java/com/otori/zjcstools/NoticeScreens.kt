package com.otori.zjcstools

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import java.time.LocalDate
@Composable
fun ExchangeCodeNoticeDialog(
    notices: List<ExchangeCodeNotice>,
    onCopy: (String) -> Unit,
    onNeverRemind: () -> Unit,
    onDismiss: () -> Unit
) {
    var dialogNotices by remember(notices) {
        mutableStateOf(notices)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "可用兑换码",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                dialogNotices.forEach { notice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notice.code,
                            fontSize = 16.sp,
                            color = Color(0xFF222222),
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = {
                                onCopy(notice.code)
                                dialogNotices = dialogNotices.filterNot { it.code == notice.code }
                            }
                        ) {
                            Text("一键复制")
                        }
                    }
                }

                if (dialogNotices.isEmpty()) {
                    Text(
                        text = "空",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("我知道了")
            }
        },
        dismissButton = {
            TextButton(onClick = onNeverRemind) {
                Text("不再提醒")
            }
        }
    )
}

@Composable
fun ExchangeCodeScreen(
    notices: List<ExchangeCodeNotice>,
    errorText: String?,
    onShowLocalData: () -> Unit,
    onCopy: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val prefs = remember {
        context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    }
    var menuHiddenCodes by remember {
        mutableStateOf(
            prefs.getStringSet(MENU_HIDDEN_EXCHANGE_CODES_KEY, emptySet()).orEmpty().toSet()
        )
    }
    var activeExpanded by remember { mutableStateOf(true) }
    var hiddenExpanded by remember { mutableStateOf(false) }
    var expiredExpanded by remember { mutableStateOf(false) }

    fun hideFromExchangeCodeMenu(code: String) {
        val updatedCodes = menuHiddenCodes + code
        menuHiddenCodes = updatedCodes
        prefs.edit(commit = true) {
            putStringSet(MENU_HIDDEN_EXCHANGE_CODES_KEY, updatedCodes)
        }
    }

    val activeSortedNotices = remember(notices, today) {
        notices
            .filter { notice ->
                !today.isBefore(notice.startDate) && !today.isAfter(notice.endDate)
            }
            .sortedWith(
                compareBy<ExchangeCodeNotice> { it.isLongTerm() }
                    .thenByDescending { it.startDate }
                    .thenByDescending { it.endDate }
                    .thenBy { it.code }
            )
    }
    val activeNotices = remember(activeSortedNotices, menuHiddenCodes) {
        activeSortedNotices.filter { it.code !in menuHiddenCodes }
    }
    val hiddenActiveNotices = remember(activeSortedNotices, menuHiddenCodes) {
        activeSortedNotices.filter { it.code in menuHiddenCodes }
    }
    val recentExpiredNotices = remember(notices, today) {
        notices
            .filter { notice ->
                notice.endDate.isBefore(today)
            }
            .sortedByDescending { it.endDate }
    }

    SecondaryHomeScreen(
        title = EXCHANGE_CODE_CARD_TITLE,
        subtitle = "",
        onBack = onBack
    ) {
        if (errorText != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A4B4B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(onClick = onShowLocalData) {
                            Text("显示本地数据")
                        }
                    }
                }
            }

            return@SecondaryHomeScreen
        }

        ExchangeCodeSection(
            title = "当前可用",
            emptyText = "暂无可用兑换码",
            notices = activeNotices,
            expanded = activeExpanded,
            expired = false,
            onCopy = onCopy,
            onToggle = { activeExpanded = !activeExpanded },
            onHide = { code -> hideFromExchangeCodeMenu(code) }
        )

        Spacer(modifier = Modifier.height(22.dp))

        ExchangeCodeSection(
            title = "已隐藏",
            emptyText = "暂无已隐藏兑换码",
            notices = hiddenActiveNotices,
            expanded = hiddenExpanded,
            expired = false,
            onCopy = onCopy,
            onToggle = { hiddenExpanded = !hiddenExpanded }
        )

        Spacer(modifier = Modifier.height(22.dp))

        ExchangeCodeSection(
            title = "已过期",
            emptyText = "暂无已过期兑换码",
            notices = recentExpiredNotices,
            expanded = expiredExpanded,
            expired = true,
            onCopy = onCopy,
            onToggle = { expiredExpanded = !expiredExpanded }
        )
    }
}

@Composable
fun ExchangeCodeSection(
    title: String,
    emptyText: String,
    notices: List<ExchangeCodeNotice>,
    expanded: Boolean,
    expired: Boolean,
    onCopy: (String) -> Unit,
    onToggle: () -> Unit,
    onHide: ((String) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title（${notices.size}）",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (expanded) "收起" else "展开",
            fontSize = 14.sp,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (!expanded) {
        return
    }

    if (notices.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Text(
                text = emptyText,
                fontSize = 15.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(18.dp)
            )
        }
    } else {
        notices.forEach { notice ->
            ExchangeCodeListCard(
                notice = notice,
                expired = expired,
                onCopy = onCopy,
                onHide = onHide
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ExchangeCodeListCard(
    notice: ExchangeCodeNotice,
    expired: Boolean,
    onCopy: (String) -> Unit,
    onHide: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notice.code,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (expired) Color(0xFF777777) else Color(0xFF222222),
                    modifier = Modifier.weight(1f)
                )

                if (!expired) {
                    TextButton(onClick = { onCopy(notice.code) }) {
                        Text("一键复制")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expired) {
                        "已于 ${notice.endDate} 过期"
                    } else if (notice.isLongTerm()) {
                        "可用日期：长期"
                    } else {
                        "过期日期：${notice.endDate}"
                    },
                    fontSize = 14.sp,
                    color = if (expired) Color(0xFF9A4B4B) else Color(0xFF4B6F3A),
                    modifier = Modifier.weight(1f)
                )

                if (!expired && onHide != null) {
                    TextButton(onClick = { onHide(notice.code) }) {
                        Text("不再提醒")
                    }
                }
            }
        }
    }
}

@Composable
fun UpdatePreviewHomeScreen(
    notices: List<UpdatePreviewNotice>,
    errorText: String?,
    onShowLocalData: () -> Unit,
    onNoticeClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val sortedNotices = remember(notices) {
        notices.sortedByDescending { it.date }
    }
    var visibleCount by remember { mutableIntStateOf(10.coerceAtMost(sortedNotices.size)) }
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState, visibleCount) {
        snapshotFlow { scrollState.value to scrollState.maxValue }
            .collect { (scrollValue, maxValue) ->
                if (visibleCount < sortedNotices.size &&
                    maxValue > 0 &&
                    scrollValue >= maxValue - 120
                ) {
                    visibleCount = (visibleCount + 10).coerceAtMost(sortedNotices.size)
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .safeDrawingPadding()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            StrokeText(
                text = UPDATE_PREVIEW_CARD_TITLE,
                fontSize = 40,
                fillColor = Color.White,
                strokeColor = Color(0xFF202020),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            StrokeText(
                text = "公告列表",
                fontSize = 24,
                fillColor = Color.White,
                strokeColor = Color(0xFF202020)
            )

            Spacer(modifier = Modifier.height(60.dp))

            if (errorText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A4B4B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(onClick = onShowLocalData) {
                            Text("显示本地数据")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            } else if (sortedNotices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = "暂无公告",
                        fontSize = 15.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(18.dp)
                    )
                }
            } else {
                sortedNotices.take(visibleCount).forEach { notice ->
                    NoticeListCard(
                        title = notice.title,
                        date = notice.date,
                        summary = notice.summary,
                        onClick = { onNoticeClick(notice.id) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            if (errorText == null && visibleCount < sortedNotices.size) {
                Text(
                    text = "继续下滑加载更早公告",
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        TopBackButton(onBack = onBack)
    }
}

@Composable
fun UpdatePreviewDetailScreen(
    notice: UpdatePreviewNotice,
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "更新公告",
        subtitle = notice.date,
        onBack = onBack,
        pinnedTitleBar = true
    ) {
        NoticeDetailCard(
            title = notice.title,
            date = notice.date,
            body = notice.body
        )
    }
}

@Composable
fun NoticeListCard(
    title: String,
    date: String,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "公告",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3D6DB5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = Color(0xFF777777)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summary,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = Color(0xFF555555)
            )
        }
    }
}

@Composable
fun NoticeDetailCard(
    title: String,
    date: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = title,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202020)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "发布时间：$date",
                fontSize = 14.sp,
                color = Color(0xFF777777)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE2E4E8))
            )

            Spacer(modifier = Modifier.height(18.dp))

            body.split("\n\n").forEachIndexed { index, paragraph ->
                Text(
                    text = paragraph,
                    fontSize = 17.sp,
                    lineHeight = 26.sp,
                    color = Color(0xFF333333)
                )

                if (index != body.split("\n\n").lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

