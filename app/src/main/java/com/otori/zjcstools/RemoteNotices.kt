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
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
const val GAME_INFO_CARD_TITLE = "游戏信息"
const val GAME_INFO_CARD_SUBTITLE = "兑换码和游戏公告"
const val OFFICIAL_NOTICE_CARD_TITLE = "正式服公告"
const val OFFICIAL_NOTICE_CARD_SUBTITLE = "查看正式服版本更新公告"
const val UPDATE_PREVIEW_CARD_TITLE = "先遣服公告"
const val UPDATE_PREVIEW_CARD_SUBTITLE = "查看先遣服版本更新公告和调整预告"
const val EXCHANGE_CODE_CARD_TITLE = "兑换码"
const val EXCHANGE_CODE_CARD_SUBTITLE = "查看当前可用兑换码和近期过期记录"
const val REMOTE_EXCHANGE_CODES_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/DHM_codes.json"
const val REMOTE_UPDATE_PREVIEW_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/XQF_Announcements.json"
const val REMOTE_OFFICIAL_NOTICE_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/ZSF_Announcements.json"
const val REMOTE_APP_UPDATE_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/app_update.json"
const val REMOTE_APP_CONFIG_URL =
    "https://zjcs-tools-otori-database.oss-cn-shanghai.aliyuncs.com/app_config.json"
const val BUNDLED_EXCHANGE_CODES_FILE = "DHM_codes.json"
const val BUNDLED_UPDATE_PREVIEW_FILE = "XQF_Announcements.json"
const val BUNDLED_OFFICIAL_NOTICE_FILE = "ZSF_Announcements.json"
const val BUNDLED_APP_CONFIG_FILE = "app_config.json"

data class UpdatePreviewNotice(
    val id: String,
    val title: String,
    val date: String,
    val summary: String,
    val body: String
)

data class ExchangeCodeReward(
    val name: String,
    val quantity: Int? = null
)

data class ExchangeCodeNotice(
    val code: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val rewards: List<ExchangeCodeReward> = emptyList()
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

data class RemoteDataFileConfig(
    val url: String,
    val version: Int
)

data class RemoteAppConfig(
    val appUpdate: AppUpdateInfo?,
    val dataFiles: Map<String, RemoteDataFileConfig>
)

fun ExchangeCodeNotice.isLongTerm(): Boolean {
    return endDate.year >= 2099
}

const val HIDDEN_EXCHANGE_CODES_KEY = "hidden_exchange_codes"
const val MENU_HIDDEN_EXCHANGE_CODES_KEY = "menu_hidden_exchange_codes"
const val CACHED_EXCHANGE_CODES_JSON_KEY = "cached_exchange_codes_json"
const val CACHED_UPDATE_PREVIEW_JSON_KEY = "cached_update_preview_json"
const val CACHED_OFFICIAL_NOTICE_JSON_KEY = "cached_official_notice_json"
const val CACHED_EXCHANGE_CODES_VERSION_KEY = "cached_exchange_codes_version"
const val CACHED_UPDATE_PREVIEW_VERSION_KEY = "cached_update_preview_version"
const val CACHED_OFFICIAL_NOTICE_VERSION_KEY = "cached_official_notice_version"
const val REMOTE_DATA_EXCHANGE_CODES_KEY = "exchangeCodes"
const val REMOTE_DATA_UPDATE_PREVIEW_KEY = "updatePreviewNotices"
const val REMOTE_DATA_OFFICIAL_NOTICES_KEY = "officialNotices"
const val REMOTE_DATA_DUNGEON_DETAILS_KEY = "dungeonDetails"
const val REMOTE_DATA_MONSTER_DETAILS_KEY = "monsterDetails"

class RemoteFileUnavailableException(message: String) : Exception(message)

val exchangeCodeNotices = emptyList<ExchangeCodeNotice>()

val updatePreviewNotices = emptyList<UpdatePreviewNotice>()

val officialNotices = emptyList<UpdatePreviewNotice>()

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

fun parseExchangeCodeRewards(item: JSONObject): List<ExchangeCodeReward> {
    val rewards = item.optJSONArray("rewards")
        ?: item.optJSONArray("rewardItems")
        ?: item.optJSONArray("prizes")
        ?: return emptyList()

    return buildList {
        for (index in 0 until rewards.length()) {
            when (val reward = rewards.opt(index)) {
                is JSONObject -> {
                    val name = reward.firstText("name", "reward", "item", "title") ?: continue
                    val quantity = when {
                        reward.has("quantity") -> reward.optInt("quantity")
                        reward.has("count") -> reward.optInt("count")
                        reward.has("amount") -> reward.optInt("amount")
                        else -> null
                    }?.takeIf { it > 0 }

                    add(ExchangeCodeReward(name = name, quantity = quantity))
                }

                is String -> {
                    val name = reward.trim()
                    if (name.isNotEmpty()) {
                        add(ExchangeCodeReward(name = name))
                    }
                }
            }
        }
    }
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
                        ),
                        rewards = parseExchangeCodeRewards(item)
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

fun parseAppUpdateInfo(root: JSONObject): AppUpdateInfo? {
    return parseAppUpdateInfo(root.toString())
}

fun parseRemoteAppConfig(rawJson: String): RemoteAppConfig? {
    return runCatching {
        val root = JSONObject(rawJson.trim())
        val appUpdate = root.optJSONObject("appUpdate")
            ?.let(::parseAppUpdateInfo)
            ?: root.optJSONObject("apkUpdate")?.let(::parseAppUpdateInfo)

        val dataFilesObject = root.optJSONObject("dataFiles") ?: JSONObject()
        val dataFiles = buildMap {
            val keys = dataFilesObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = dataFilesObject.optJSONObject(key) ?: continue
                val url = item.firstText("url", "downloadUrl").orEmpty()
                val version = item.optInt("version", 0)
                if (url.isNotBlank() && version > 0) {
                    put(key, RemoteDataFileConfig(url = url, version = version))
                }
            }
        }

        RemoteAppConfig(appUpdate = appUpdate, dataFiles = dataFiles)
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
        val remoteInfo = fetchRemoteAppConfig()
            ?.appUpdate
            ?: downloadText(REMOTE_APP_UPDATE_URL).let(::parseAppUpdateInfo)
            ?: return@runCatching null

        remoteInfo.takeIf { it.versionCode > currentAppVersionCode(context) }
    }.getOrNull()
}

suspend fun fetchRemoteAppConfig(): RemoteAppConfig? {
    return runCatching {
        downloadText(REMOTE_APP_CONFIG_URL).let(::parseRemoteAppConfig)
    }.getOrNull()
}

fun loadBundledAppConfig(context: Context): RemoteAppConfig? {
    return readBundledJson(context, BUNDLED_APP_CONFIG_FILE)
        ?.let(::parseRemoteAppConfig)
}

suspend fun remoteDataFileConfig(
    context: Context,
    key: String,
    fallbackUrl: String
): RemoteDataFileConfig {
    return fetchRemoteAppConfig()
        ?.dataFiles
        ?.get(key)
        ?: loadBundledAppConfig(context)
            ?.dataFiles
            ?.get(key)
        ?: RemoteDataFileConfig(url = fallbackUrl, version = 0)
}

fun loadCachedExchangeCodeNotices(context: Context): List<ExchangeCodeNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val localJson = readLocalRemoteJson(context, BUNDLED_EXCHANGE_CODES_FILE)
    val legacyCachedJson = prefs.getString(CACHED_EXCHANGE_CODES_JSON_KEY, null)
    if (localJson.isNullOrBlank() && !legacyCachedJson.isNullOrBlank()) {
        writeLocalRemoteJson(context, BUNDLED_EXCHANGE_CODES_FILE, legacyCachedJson)
    }
    val cachedJson = localJson ?: legacyCachedJson

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

fun loadBundledOfficialNotices(context: Context): List<UpdatePreviewNotice>? {
    return readBundledJson(context, BUNDLED_OFFICIAL_NOTICE_FILE)
        ?.let(::parseUpdatePreviewNotices)
}

fun readBundledJson(context: Context, fileName: String): String? {
    return runCatching {
        context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }.getOrNull()
}

fun readLocalRemoteJson(context: Context, fileName: String): String? {
    return runCatching {
        File(context.filesDir, fileName)
            .takeIf { it.exists() && it.isFile }
            ?.readText(Charsets.UTF_8)
    }.getOrNull()
}

fun writeLocalRemoteJson(context: Context, fileName: String, rawJson: String) {
    runCatching {
        File(context.filesDir, fileName).writeText(rawJson, Charsets.UTF_8)
    }
}

fun loadCachedUpdatePreviewNotices(context: Context): List<UpdatePreviewNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val localJson = readLocalRemoteJson(context, BUNDLED_UPDATE_PREVIEW_FILE)
    val legacyCachedJson = prefs.getString(CACHED_UPDATE_PREVIEW_JSON_KEY, null)
    if (localJson.isNullOrBlank() && !legacyCachedJson.isNullOrBlank()) {
        writeLocalRemoteJson(context, BUNDLED_UPDATE_PREVIEW_FILE, legacyCachedJson)
    }
    val cachedJson = localJson ?: legacyCachedJson

    return cachedJson
        ?.let(::parseUpdatePreviewNotices)
        ?.takeIf { it.isNotEmpty() }
        ?: loadBundledUpdatePreviewNotices(context)
        ?: updatePreviewNotices
}

fun loadCachedOfficialNotices(context: Context): List<UpdatePreviewNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    val localJson = readLocalRemoteJson(context, BUNDLED_OFFICIAL_NOTICE_FILE)
    val legacyCachedJson = prefs.getString(CACHED_OFFICIAL_NOTICE_JSON_KEY, null)
    if (localJson.isNullOrBlank() && !legacyCachedJson.isNullOrBlank()) {
        writeLocalRemoteJson(context, BUNDLED_OFFICIAL_NOTICE_FILE, legacyCachedJson)
    }
    val cachedJson = localJson ?: legacyCachedJson

    return cachedJson
        ?.let(::parseUpdatePreviewNotices)
        ?: loadBundledOfficialNotices(context)
        ?: officialNotices
}

suspend fun loadRemoteExchangeCodeNotices(context: Context): List<ExchangeCodeNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val config = remoteDataFileConfig(context, REMOTE_DATA_EXCHANGE_CODES_KEY, REMOTE_EXCHANGE_CODES_URL)
        if (config.version > 0 &&
            prefs.getInt(CACHED_EXCHANGE_CODES_VERSION_KEY, 0) >= config.version
        ) {
            return@runCatching loadCachedExchangeCodeNotices(context)
        }

        val rawJson = downloadText(config.url)
        val notices = parseExchangeCodeNotices(rawJson)
        if (notices.isNotEmpty()) {
            writeLocalRemoteJson(context, BUNDLED_EXCHANGE_CODES_FILE, rawJson)
            prefs.edit(commit = true) {
                putString(CACHED_EXCHANGE_CODES_JSON_KEY, rawJson)
                putInt(CACHED_EXCHANGE_CODES_VERSION_KEY, config.version)
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
        val config = remoteDataFileConfig(context, REMOTE_DATA_EXCHANGE_CODES_KEY, REMOTE_EXCHANGE_CODES_URL)
        if (config.version > 0 &&
            prefs.getInt(CACHED_EXCHANGE_CODES_VERSION_KEY, 0) >= config.version
        ) {
            return@runCatching loadCachedExchangeCodeNotices(context)
        }

        val rawJson = downloadText(config.url)
        val notices = parseExchangeCodeNotices(rawJson)

        if (notices.isEmpty()) {
            throw RemoteFileUnavailableException("Empty exchange code data")
        }

        writeLocalRemoteJson(context, BUNDLED_EXCHANGE_CODES_FILE, rawJson)
        prefs.edit(commit = true) {
            putString(CACHED_EXCHANGE_CODES_JSON_KEY, rawJson)
            putInt(CACHED_EXCHANGE_CODES_VERSION_KEY, config.version)
        }

        notices
    }
}

suspend fun loadRemoteUpdatePreviewNotices(context: Context): List<UpdatePreviewNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val config = remoteDataFileConfig(context, REMOTE_DATA_UPDATE_PREVIEW_KEY, REMOTE_UPDATE_PREVIEW_URL)
        if (config.version > 0 &&
            prefs.getInt(CACHED_UPDATE_PREVIEW_VERSION_KEY, 0) >= config.version
        ) {
            return@runCatching loadCachedUpdatePreviewNotices(context)
        }

        val rawJson = downloadText(config.url)
        val notices = parseUpdatePreviewNotices(rawJson)
        if (notices.isNotEmpty()) {
            writeLocalRemoteJson(context, BUNDLED_UPDATE_PREVIEW_FILE, rawJson)
            prefs.edit(commit = true) {
                putString(CACHED_UPDATE_PREVIEW_JSON_KEY, rawJson)
                putInt(CACHED_UPDATE_PREVIEW_VERSION_KEY, config.version)
            }
            notices
        } else {
            loadCachedUpdatePreviewNotices(context)
        }
    }.getOrElse {
        loadCachedUpdatePreviewNotices(context)
    }
}

suspend fun loadRemoteOfficialNotices(context: Context): List<UpdatePreviewNotice> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val config = remoteDataFileConfig(context, REMOTE_DATA_OFFICIAL_NOTICES_KEY, REMOTE_OFFICIAL_NOTICE_URL)
        if (config.version > 0 &&
            prefs.getInt(CACHED_OFFICIAL_NOTICE_VERSION_KEY, 0) >= config.version
        ) {
            return@runCatching loadCachedOfficialNotices(context)
        }

        val rawJson = downloadText(config.url)
        val notices = parseUpdatePreviewNotices(rawJson)
        writeLocalRemoteJson(context, BUNDLED_OFFICIAL_NOTICE_FILE, rawJson)
        prefs.edit(commit = true) {
            putString(CACHED_OFFICIAL_NOTICE_JSON_KEY, rawJson)
            putInt(CACHED_OFFICIAL_NOTICE_VERSION_KEY, config.version)
        }
        notices
    }.getOrElse {
        loadCachedOfficialNotices(context)
    }
}

suspend fun fetchRemoteUpdatePreviewNoticesStrict(context: Context): Result<List<UpdatePreviewNotice>> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val config = remoteDataFileConfig(context, REMOTE_DATA_UPDATE_PREVIEW_KEY, REMOTE_UPDATE_PREVIEW_URL)
        if (config.version > 0 &&
            prefs.getInt(CACHED_UPDATE_PREVIEW_VERSION_KEY, 0) >= config.version
        ) {
            return@runCatching loadCachedUpdatePreviewNotices(context)
        }

        val rawJson = downloadText(config.url)
        val notices = parseUpdatePreviewNotices(rawJson)

        if (notices.isEmpty()) {
            throw RemoteFileUnavailableException("Empty update preview data")
        }

        writeLocalRemoteJson(context, BUNDLED_UPDATE_PREVIEW_FILE, rawJson)
        prefs.edit(commit = true) {
            putString(CACHED_UPDATE_PREVIEW_JSON_KEY, rawJson)
            putInt(CACHED_UPDATE_PREVIEW_VERSION_KEY, config.version)
        }

        notices
    }
}

suspend fun fetchRemoteOfficialNoticesStrict(context: Context): Result<List<UpdatePreviewNotice>> {
    val prefs = context.getSharedPreferences("check_data", Context.MODE_PRIVATE)

    return runCatching {
        val config = remoteDataFileConfig(context, REMOTE_DATA_OFFICIAL_NOTICES_KEY, REMOTE_OFFICIAL_NOTICE_URL)
        if (config.version > 0 &&
            prefs.getInt(CACHED_OFFICIAL_NOTICE_VERSION_KEY, 0) >= config.version
        ) {
            return@runCatching loadCachedOfficialNotices(context)
        }

        val rawJson = downloadText(config.url)
        val notices = parseUpdatePreviewNotices(rawJson)

        writeLocalRemoteJson(context, BUNDLED_OFFICIAL_NOTICE_FILE, rawJson)
        prefs.edit(commit = true) {
            putString(CACHED_OFFICIAL_NOTICE_JSON_KEY, rawJson)
            putInt(CACHED_OFFICIAL_NOTICE_VERSION_KEY, config.version)
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

