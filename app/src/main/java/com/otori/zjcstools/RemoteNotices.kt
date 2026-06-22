package com.otori.zjcstools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
const val UPDATE_PREVIEW_CARD_TITLE = "先遣服更新前瞻"
const val UPDATE_PREVIEW_CARD_SUBTITLE = "查看先遣服版本更新公告和调整预告"
const val EXCHANGE_CODE_CARD_TITLE = "兑换码"
const val EXCHANGE_CODE_CARD_SUBTITLE = "查看当前可用兑换码和近期过期记录"
const val REMOTE_EXCHANGE_CODES_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json"
const val REMOTE_UPDATE_PREVIEW_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json"
const val REMOTE_APP_UPDATE_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/app_update.json"
const val BUNDLED_EXCHANGE_CODES_FILE = "DHM_codes.json"
const val BUNDLED_UPDATE_PREVIEW_FILE = "XQF_Announcements.json"

data class UpdatePreviewNotice(
    val id: String,
    val title: String,
    val date: String,
    val summary: String,
    val body: String
)

data class ExchangeCodeNotice(
    val code: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class AppUpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val title: String,
    val message: String,
    val releaseNotes: List<String>,
    val forceUpdate: Boolean
)

fun ExchangeCodeNotice.isLongTerm(): Boolean {
    return endDate.year >= 2099
}

const val HIDDEN_EXCHANGE_CODES_KEY = "hidden_exchange_codes"
const val MENU_HIDDEN_EXCHANGE_CODES_KEY = "menu_hidden_exchange_codes"
const val CACHED_EXCHANGE_CODES_JSON_KEY = "cached_exchange_codes_json"
const val CACHED_UPDATE_PREVIEW_JSON_KEY = "cached_update_preview_json"

class RemoteFileUnavailableException(message: String) : Exception(message)

val exchangeCodeNotices = emptyList<ExchangeCodeNotice>()

val updatePreviewNotices = emptyList<UpdatePreviewNotice>()

fun JSONObject.firstText(vararg names: String): String? {
    names.forEach { name ->
        val value = optString(name).trim()
        if (value.isNotEmpty()) {
            return value
        }
    }

    return null
}

fun parseRemoteDate(text: String?, fallback: LocalDate): LocalDate {
    return runCatching {
        LocalDate.parse(text.orEmpty().trim())
    }.getOrDefault(fallback)
}

fun remoteJsonArray(rawJson: String, vararg arrayKeys: String): JSONArray {
    val trimmed = rawJson.trim()

    if (trimmed.startsWith("[")) {
        return JSONArray(trimmed)
    }

    val root = JSONObject(trimmed)
    arrayKeys.forEach { key ->
        root.optJSONArray(key)?.let { return it }
    }

    return JSONArray()
}

fun parseExchangeCodeNotices(rawJson: String): List<ExchangeCodeNotice> {
    return runCatching {
        val items = remoteJsonArray(rawJson, "codes", "items", "data", "list")

        buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val code = item.firstText("code", "exchangeCode", "giftCode", "value") ?: continue

                add(
                    ExchangeCodeNotice(
                        code = code,
                        startDate = parseRemoteDate(
                            item.firstText("startDate", "start", "beginDate"),
                            LocalDate.of(1970, 1, 1)
                        ),
                        endDate = parseRemoteDate(
                            item.firstText("endDate", "expireDate", "expiresAt", "end"),
                            LocalDate.of(2099, 12, 31)
                        )
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

fun parseUpdatePreviewNotices(rawJson: String): List<UpdatePreviewNotice> {
    return runCatching {
        val items = remoteJsonArray(rawJson, "announcements", "notices", "items", "data", "list")

        buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val title = item.firstText("title", "name") ?: continue
                val date = item.firstText("date", "publishDate", "startDate", "time").orEmpty()
                val body = item.firstText("body", "content", "detail", "details", "text")
                    ?: item.firstText("summary", "description")
                    ?: ""

                add(
                    UpdatePreviewNotice(
                        id = item.firstText("id")
                            ?: "${date.ifEmpty { "remote" }}-$index",
                        title = title,
                        date = date,
                        summary = item.firstText("summary", "description")
                            ?: body.lineSequence().firstOrNull().orEmpty(),
                        body = body
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

fun parseAppUpdateInfo(rawJson: String): AppUpdateInfo? {
    return runCatching {
        val root = JSONObject(rawJson.trim())
        val enabled = root.optBoolean("enabled", true)
        val versionCode = root.optLong("versionCode", 0L)
        val apkUrl = root.firstText("apkUrl", "downloadUrl", "url").orEmpty()

        if (!enabled || versionCode <= 0L || apkUrl.isBlank()) {
            return@runCatching null
        }

        val releaseNotes = buildList {
            val notesArray = root.optJSONArray("releaseNotes")
            if (notesArray != null) {
                for (index in 0 until notesArray.length()) {
                    val note = notesArray.optString(index).trim()
                    if (note.isNotEmpty()) {
                        add(note)
                    }
                }
            } else {
                root.optString("releaseNotes")
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach(::add)
            }
        }

        AppUpdateInfo(
            versionCode = versionCode,
            versionName = root.firstText("versionName", "version").orEmpty(),
            apkUrl = apkUrl,
            title = root.firstText("title").orEmpty().ifBlank { "发现新版本" },
            message = root.firstText("message", "description").orEmpty(),
            releaseNotes = releaseNotes,
            forceUpdate = root.optBoolean("forceUpdate", false)
        )
    }.getOrNull()
}

suspend fun downloadText(url: String): String = withContext(Dispatchers.IO) {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 8000
        readTimeout = 8000
        requestMethod = "GET"
    }

    try {
        if (connection.responseCode !in 200..299) {
            throw RemoteFileUnavailableException("HTTP ${connection.responseCode}")
        }

        connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

fun currentAppVersionCode(context: Context): Long {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }
}

suspend fun fetchAppUpdateInfo(context: Context): AppUpdateInfo? {
    return runCatching {
        val remoteInfo = downloadText(REMOTE_APP_UPDATE_URL)
            .let(::parseAppUpdateInfo)
            ?: return@runCatching null

        remoteInfo.takeIf { it.versionCode > currentAppVersionCode(context) }
    }.getOrNull()
}

fun loadCachedExchangeCodeNotices(context: Context): List<ExchangeCodeNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val cachedJson = prefs.getString(CACHED_EXCHANGE_CODES_JSON_KEY, null)

    return cachedJson
        ?.let(::parseExchangeCodeNotices)
        ?.takeIf { it.isNotEmpty() }
        ?: loadBundledExchangeCodeNotices(context)
        ?: exchangeCodeNotices
}

fun loadBundledExchangeCodeNotices(context: Context): List<ExchangeCodeNotice>? {
    return readBundledJson(context, BUNDLED_EXCHANGE_CODES_FILE)
        ?.let(::parseExchangeCodeNotices)
        ?.takeIf { it.isNotEmpty() }
}

fun loadBundledUpdatePreviewNotices(context: Context): List<UpdatePreviewNotice>? {
    return readBundledJson(context, BUNDLED_UPDATE_PREVIEW_FILE)
        ?.let(::parseUpdatePreviewNotices)
        ?.takeIf { it.isNotEmpty() }
}

fun readBundledJson(context: Context, fileName: String): String? {
    return runCatching {
        context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }.getOrNull()
}

fun loadCachedUpdatePreviewNotices(context: Context): List<UpdatePreviewNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val cachedJson = prefs.getString(CACHED_UPDATE_PREVIEW_JSON_KEY, null)

    return cachedJson
        ?.let(::parseUpdatePreviewNotices)
        ?.takeIf { it.isNotEmpty() }
        ?: loadBundledUpdatePreviewNotices(context)
        ?: updatePreviewNotices
}

suspend fun loadRemoteExchangeCodeNotices(context: Context): List<ExchangeCodeNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val rawJson = downloadText(REMOTE_EXCHANGE_CODES_URL)
        val notices = parseExchangeCodeNotices(rawJson)
        if (notices.isNotEmpty()) {
            prefs.edit(commit = true) {
                putString(CACHED_EXCHANGE_CODES_JSON_KEY, rawJson)
            }
            notices
        } else {
            loadCachedExchangeCodeNotices(context)
        }
    }.getOrElse {
        loadCachedExchangeCodeNotices(context)
    }
}

suspend fun fetchRemoteExchangeCodeNoticesStrict(context: Context): Result<List<ExchangeCodeNotice>> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val rawJson = downloadText(REMOTE_EXCHANGE_CODES_URL)
        val notices = parseExchangeCodeNotices(rawJson)

        if (notices.isEmpty()) {
            throw RemoteFileUnavailableException("Empty exchange code data")
        }

        prefs.edit(commit = true) {
            putString(CACHED_EXCHANGE_CODES_JSON_KEY, rawJson)
        }

        notices
    }
}

suspend fun loadRemoteUpdatePreviewNotices(context: Context): List<UpdatePreviewNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val rawJson = downloadText(REMOTE_UPDATE_PREVIEW_URL)
        val notices = parseUpdatePreviewNotices(rawJson)
        if (notices.isNotEmpty()) {
            prefs.edit(commit = true) {
                putString(CACHED_UPDATE_PREVIEW_JSON_KEY, rawJson)
            }
            notices
        } else {
            loadCachedUpdatePreviewNotices(context)
        }
    }.getOrElse {
        loadCachedUpdatePreviewNotices(context)
    }
}

suspend fun fetchRemoteUpdatePreviewNoticesStrict(context: Context): Result<List<UpdatePreviewNotice>> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val rawJson = downloadText(REMOTE_UPDATE_PREVIEW_URL)
        val notices = parseUpdatePreviewNotices(rawJson)

        if (notices.isEmpty()) {
            throw RemoteFileUnavailableException("Empty update preview data")
        }

        prefs.edit(commit = true) {
            putString(CACHED_UPDATE_PREVIEW_JSON_KEY, rawJson)
        }

        notices
    }
}

fun activeExchangeCodeNotices(
    context: Context,
    today: LocalDate,
    notices: List<ExchangeCodeNotice> = exchangeCodeNotices
): List<ExchangeCodeNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val hiddenCodes = prefs.getStringSet(HIDDEN_EXCHANGE_CODES_KEY, emptySet()).orEmpty()

    return notices.filter { notice ->
        notice.code !in hiddenCodes &&
                !today.isBefore(notice.startDate) &&
                !today.isAfter(notice.endDate)
    }
}

fun hideExchangeCodeNotices(context: Context, notices: List<ExchangeCodeNotice>) {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val hiddenCodes = prefs.getStringSet(HIDDEN_EXCHANGE_CODES_KEY, emptySet()).orEmpty().toMutableSet()
    hiddenCodes.addAll(notices.map { it.code })

    prefs.edit(commit = true) {
        putStringSet(HIDDEN_EXCHANGE_CODES_KEY, hiddenCodes)
    }
}

fun copyExchangeCode(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("exchange_code", code))
    Toast.makeText(context, "已复制兑换码", Toast.LENGTH_SHORT).show()
}

