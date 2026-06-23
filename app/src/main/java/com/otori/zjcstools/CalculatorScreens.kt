package com.otori.zjcstools

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
@Composable
fun DungeonMorningStarScreen(
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "副本晨星计算器",
        subtitle = "根据原初档位、幸运神像和工资装计算收益",
        onBack = onBack,
        pinnedTitleBar = true
    ) {
        DungeonMorningStarCalculator()
    }
}

private const val ASTRAL_BLUE_GIFT_EXP = 160
private const val ASTRAL_PURPLE_GIFT_EXP = 800
private const val ASTRAL_ORANGE_GIFT_EXP = 2400
private const val ASTRAL_PRE_LEVEL_100_BONUS_NUMERATOR = 5
private const val ASTRAL_PRE_LEVEL_100_BONUS_DENOMINATOR = 4
private const val ASTRAL_DIVINE_ASTROLABE_EXP = 9600
private const val ASTRAL_DIVINE_ASTROLABE_LIMIT = 132
private const val ASTRAL_OVERFLOW_GIFT_EXP_TEXT_LIMIT = 960000L

private val astralKamiLevelExpTable = listOf(
    2 to 900,
    3 to 1050,
    4 to 1200,
    5 to 1350,
    6 to 1500,
    7 to 1530,
    8 to 1560,
    9 to 1590,
    10 to 1620,
    11 to 1680,
    12 to 1740,
    13 to 1800,
    14 to 1860,
    15 to 1920,
    16 to 1980,
    17 to 2040,
    18 to 2100,
    19 to 2160,
    20 to 2220,
    21 to 2310,
    22 to 2400,
    23 to 2490,
    24 to 2580,
    25 to 2670,
    26 to 2760,
    27 to 2880,
    28 to 3000,
    29 to 3120,
    30 to 3240,
    31 to 3330,
    32 to 3420,
    33 to 3510,
    34 to 3600,
    35 to 3720,
    36 to 3840,
    37 to 3960,
    38 to 4080,
    39 to 4200,
    40 to 4320,
    41 to 4440,
    42 to 4560,
    43 to 4680,
    44 to 4800,
    45 to 4920,
    46 to 5040,
    47 to 5160,
    48 to 5280,
    49 to 5400,
    50 to 5520,
    51 to 5640,
    52 to 5760,
    53 to 5910,
    54 to 6060,
    55 to 6210,
    56 to 6360,
    57 to 6510,
    58 to 6660,
    59 to 6810,
    60 to 6960,
    61 to 7110,
    62 to 7260,
    63 to 7410,
    64 to 7560,
    65 to 7710,
    66 to 7860,
    67 to 8040,
    68 to 8220,
    69 to 8400,
    70 to 8580,
    71 to 8760,
    72 to 8940,
    73 to 9120,
    74 to 9300,
    75 to 9480,
    76 to 9660,
    77 to 9840,
    78 to 10020,
    79 to 10230,
    80 to 10440,
    81 to 10650,
    82 to 10860,
    83 to 11070,
    84 to 11280,
    85 to 11490,
    86 to 11700,
    87 to 11910,
    88 to 12120,
    89 to 12360,
    90 to 12600,
    91 to 12840,
    92 to 13080,
    93 to 13320,
    94 to 13560,
    95 to 13800,
    96 to 14040,
    97 to 14280,
    98 to 14550,
    99 to 14820,
    100 to 29997
)

private val astralKamiTotalExpTo100 = astralKamiLevelExpTable.sumOf { it.second }

data class AstralKamiResult(
    val giftExp: Long,
    val progressPercent: Double,
    val finalLevel: Int,
    val divineAstrolabe: Int,
    val overflowGiftText: String?
)

data class AssaultArmorBreakResult(
    val baseDamageCoefficient: Double,
    val zeroBuffCoefficient: Double,
    val zeroBuffDamageIncrease: Double,
    val threeBuffCoefficient: Double,
    val threeBuffDamageIncrease: Double
)

data class AssaultArmorBreakOption(
    val quality: String,
    val percentText: String,
    val percent: Double,
    val iconRes: Int
)

private val assaultArmorBreakOptions = listOf(
    AssaultArmorBreakOption("彩-不朽", "46.1%", 0.461, R.drawable.qxpj_1_bx),
    AssaultArmorBreakOption("红-神话", "39.6%", 0.396, R.drawable.qxpj_2_sh),
    AssaultArmorBreakOption("金-奇迹", "33%", 0.33, R.drawable.qxpj_3_qj),
    AssaultArmorBreakOption("橙-传说", "26.4%", 0.264, R.drawable.qxpj_4_cs)
)

private const val ASSAULT_ARMOR_BREAK_ATTACK_KEY = "assault_armor_break_attack"
private const val ASSAULT_ARMOR_BREAK_DEFENSE_KEY = "assault_armor_break_defense"
private const val ASSAULT_ARMOR_BREAK_FLAT_KEY = "assault_armor_break_flat"
private const val ASSAULT_ARMOR_BREAK_OPTION_INDEX_KEY = "assault_armor_break_option_index"

data class CommissionDungeonNode(
    val id: String,
    val label: String,
    val row: Int,
    val columnSlot: Int,
    val type: CommissionDungeonNodeType = CommissionDungeonNodeType.Normal,
    val monsterSpawns: List<CommissionMonsterSpawn> = emptyList()
)

data class CommissionDungeonRoute(
    val fromId: String,
    val toId: String
)

data class CommissionMonster(
    val displayName: String,
    val variableName: String,
    val pinyinName: String
)

data class CommissionMonsterSpawn(
    val monsterVariableName: String,
    val count: Int
)

data class CommissionMonsterRequirement(
    val monsterVariableName: String,
    val requiredCount: Int
)

data class CommissionDungeonMap(
    val name: String,
    val variableName: String,
    val nodes: List<CommissionDungeonNode>,
    val routes: List<CommissionDungeonRoute>
)

data class CommissionDungeonRoutePlan(
    val nodePath: List<String>,
    val targetNodeIdsByMonster: Map<String, Set<String>>,
    val matchedCountByMonster: Map<String, Int>
)

data class CommissionDungeonRouteResult(
    val routeKey: String,
    val dungeonMap: CommissionDungeonMap,
    val routePlan: CommissionDungeonRoutePlan,
    val requirements: List<CommissionMonsterRequirement>
)

enum class CommissionDungeonNodeType {
    Normal,
    Creep,
    Elite,
    Boss,
    Camp,
    Destination
}

private const val COMMISSION_DUNGEON_DIFFICULTY_NORMAL_HARD_SUFFIX = "nh"
private const val COMMISSION_DUNGEON_DIFFICULTY_NIGHTMARE_SUFFIX = "nightmare"
private const val COMMISSION_DUNGEON_DIFFICULTY_HELL_SUFFIX = "hell"
private const val COMMISSION_DUNGEON_DIFFICULTY_ABYSS_SUFFIX = "abyss"
private const val COMMISSION_DUNGEON_REGION_SENGUO = "senguo"
private const val COMMISSION_DUNGEON_REGION_SHANGUO = "shanguo"
private const val COMMISSION_DUNGEON_REGION_ZEGUO = "zeguo"
private const val COMMISSION_DUNGEON_REGION_LONGGUO = "longguo"
private const val COMMISSION_DUNGEON_REGION_YUGUO = "yuguo"
private const val COMMISSION_DUNGEON_REGION_HAPADI = "hapadi"
private const val COMMISSION_DUNGEON_REGION_YIGENISI = "yigenisi"
private const val COMMISSION_DUNGEON_INSTANCE_SJZS = "sjzs"

private val commissionMonsters = listOf(
    CommissionMonster("野猪", commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "yezhu"), "yezhu"),
    CommissionMonster("臭弹花", commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "choudanhua"), "choudanhua"),
    CommissionMonster("森狼打手", commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "senlangdashou"), "senlangdashou"),
    CommissionMonster("森狼投手", commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "senlangtoushou"), "senlangtoushou"),
    CommissionMonster("飞镰螳螂", commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "feiliantanglang"), "feiliantanglang"),
    CommissionMonster("水丝蜘蛛", commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "shuisizhizhu"), "shuisizhizhu")
)

private val commissionMonsterByVariableName = commissionMonsters.associateBy { it.variableName }

private val commissionDungeonRegionNameByCode = mapOf(
    COMMISSION_DUNGEON_REGION_SENGUO to "森之国",
    COMMISSION_DUNGEON_REGION_SHANGUO to "山之国",
    COMMISSION_DUNGEON_REGION_ZEGUO to "泽之国",
    COMMISSION_DUNGEON_REGION_LONGGUO to "龙之国",
    COMMISSION_DUNGEON_REGION_YUGUO to "羽之国",
    COMMISSION_DUNGEON_REGION_HAPADI to "哈帕迪",
    COMMISSION_DUNGEON_REGION_YIGENISI to "伊格尼斯"
)

private val commissionDungeonInstanceNameByCode = mapOf(
    COMMISSION_DUNGEON_INSTANCE_SJZS to "世界之树"
)

private val commissionDungeonDifficultyNameBySuffix = mapOf(
    COMMISSION_DUNGEON_DIFFICULTY_NORMAL_HARD_SUFFIX to "普通/困难",
    COMMISSION_DUNGEON_DIFFICULTY_NIGHTMARE_SUFFIX to "噩梦",
    COMMISSION_DUNGEON_DIFFICULTY_HELL_SUFFIX to "炼狱",
    COMMISSION_DUNGEON_DIFFICULTY_ABYSS_SUFFIX to "深渊"
)

private val commissionDungeonMaps = listOf(
    CommissionDungeonMap(
        name = "世界之树 普通/困难",
        variableName = "instance_${COMMISSION_DUNGEON_REGION_SENGUO}_${COMMISSION_DUNGEON_INSTANCE_SJZS}_$COMMISSION_DUNGEON_DIFFICULTY_NORMAL_HARD_SUFFIX",
        nodes = listOf(
            CommissionDungeonNode(
                id = "szg_12",
                label = "终点",
                row = 0,
                columnSlot = 3,
                type = CommissionDungeonNodeType.Destination
            ),
            CommissionDungeonNode(
                id = "szg_11",
                label = "首领",
                row = 1,
                columnSlot = 3,
                type = CommissionDungeonNodeType.Boss
            ),
            CommissionDungeonNode(
                id = "szg_10",
                label = "休息点",
                row = 2,
                columnSlot = 3,
                type = CommissionDungeonNodeType.Camp
            ),
            CommissionDungeonNode(
                id = "szg_8",
                label = "大怪",
                row = 3,
                columnSlot = 2,
                type = CommissionDungeonNodeType.Elite
            ),
            CommissionDungeonNode(
                id = "szg_9",
                label = "小怪",
                row = 3,
                columnSlot = 4,
                type = CommissionDungeonNodeType.Creep,
                monsterSpawns = listOf(
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "feiliantanglang"), 9),
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "shuisizhizhu"), 6)
                )
            ),
            CommissionDungeonNode(
                id = "szg_6",
                label = "小怪",
                row = 4,
                columnSlot = 2,
                type = CommissionDungeonNodeType.Creep,
                monsterSpawns = listOf(
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "senlangdashou"), 9),
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "senlangtoushou"), 6)
                )
            ),
            CommissionDungeonNode(
                id = "szg_7",
                label = "休息点",
                row = 4,
                columnSlot = 3,
                type = CommissionDungeonNodeType.Camp
            ),
            CommissionDungeonNode(
                id = "szg_4",
                label = "首领",
                row = 5,
                columnSlot = 3,
                type = CommissionDungeonNodeType.Boss
            ),
            CommissionDungeonNode(
                id = "szg_5",
                label = "精英",
                row = 5,
                columnSlot = 4,
                type = CommissionDungeonNodeType.Elite,
                monsterSpawns = listOf(
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "yezhu"), 3),
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "choudanhua"), 3)
                )
            ),
            CommissionDungeonNode(
                id = "szg_2",
                label = "小怪",
                row = 6,
                columnSlot = 2,
                type = CommissionDungeonNodeType.Creep,
                monsterSpawns = listOf(
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "yezhu"), 9),
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "choudanhua"), 3)
                )
            ),
            CommissionDungeonNode(
                id = "szg_3",
                label = "大怪",
                row = 6,
                columnSlot = 4,
                type = CommissionDungeonNodeType.Elite,
                monsterSpawns = listOf(
                    CommissionMonsterSpawn(commissionMonsterVariableName(COMMISSION_DUNGEON_REGION_SENGUO, "yezhu"), 6)
                )
            ),
            CommissionDungeonNode(
                id = "szg_1",
                label = "底部营地",
                row = 7,
                columnSlot = 3,
                type = CommissionDungeonNodeType.Camp
            )
        ),
        routes = listOf(
            CommissionDungeonRoute("szg_1", "szg_2"),
            CommissionDungeonRoute("szg_1", "szg_3"),
            CommissionDungeonRoute("szg_2", "szg_4"),
            CommissionDungeonRoute("szg_3", "szg_4"),
            CommissionDungeonRoute("szg_3", "szg_5"),
            CommissionDungeonRoute("szg_4", "szg_6"),
            CommissionDungeonRoute("szg_4", "szg_7"),
            CommissionDungeonRoute("szg_5", "szg_7"),
            CommissionDungeonRoute("szg_6", "szg_8"),
            CommissionDungeonRoute("szg_7", "szg_8"),
            CommissionDungeonRoute("szg_7", "szg_9"),
            CommissionDungeonRoute("szg_8", "szg_10"),
            CommissionDungeonRoute("szg_9", "szg_10"),
            CommissionDungeonRoute("szg_10", "szg_11"),
            CommissionDungeonRoute("szg_11", "szg_12")
        )
    )
)

@Composable
fun AssaultArmorBreakScreen(
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "强袭破甲增伤计算器",
        subtitle = "计算强袭破甲增伤收益",
        onBack = onBack,
        pinnedTitleBar = true
    ) {
        AssaultArmorBreakCalculator()
    }
}

@Composable
fun CommissionMonsterLookupScreen(
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "每日委托怪物搜索",
        subtitle = "查找每日委托所需怪物在副本内的位置",
        onBack = onBack,
        pinnedTitleBar = true
    ) {
        CommissionMonsterLookupTool()
    }
}

@Composable
fun CommissionMonsterLookupTool() {
    var monsterSearchText by remember { mutableStateOf("") }
    val selectedMonsterRequirements = remember { mutableStateListOf<CommissionMonsterRequirement>() }
    var shouldShowResults by remember { mutableStateOf(false) }
    var expandedDungeonVariableName by remember { mutableStateOf("") }
    val filteredMonsters = remember(monsterSearchText, selectedMonsterRequirements.toList()) {
        val keyword = monsterSearchText.trim()
        val selectedVariableNames = selectedMonsterRequirements.map { it.monsterVariableName }.toSet()
        if (keyword.isBlank()) {
            emptyList()
        } else {
            val normalizedKeyword = keyword.lowercase(Locale.US)
            commissionMonsters.filter { monster ->
                monster.variableName !in selectedVariableNames &&
                    (keyword in monster.displayName ||
                    normalizedKeyword in monster.pinyinName
                    )
            }
        }
    }
    val routeResults = remember(selectedMonsterRequirements.toList(), shouldShowResults) {
        if (!shouldShowResults || selectedMonsterRequirements.isEmpty()) {
            emptyList()
        } else {
            commissionDungeonMaps.flatMap { dungeonMap ->
                dungeonMap.planRouteGroupsForMonsters(selectedMonsterRequirements)
                    .mapIndexed { index, (routePlan, routeRequirements) ->
                        CommissionDungeonRouteResult(
                            routeKey = "${dungeonMap.variableName}_$index",
                            dungeonMap = dungeonMap,
                            routePlan = routePlan,
                            requirements = routeRequirements
                        )
                    }
            }
        }
    }

    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "选择任务怪物（已选 ${selectedMonsterRequirements.size}/10）",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = monsterSearchText,
                    onValueChange = {
                        monsterSearchText = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { SearchFieldIcon() },
                    label = { Text("输入怪物名") },
                    placeholder = {
                        Text(
                            text = "输入怪物名",
                            color = Color(0xFF999999)
                        )
                    }
                )

                if (monsterSearchText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            if (filteredMonsters.isEmpty()) {
                                Text(
                                    text = "未搜索到匹配怪物",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    fontSize = 14.sp,
                                    color = Color(0xFF777777)
                                )
                            } else {
                                filteredMonsters.forEach { monster ->
                                    Text(
                                        text = monster.displayName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (selectedMonsterRequirements.size < 10) {
                                                    selectedMonsterRequirements += CommissionMonsterRequirement(
                                                        monsterVariableName = monster.variableName,
                                                        requiredCount = 3
                                                    )
                                                }
                                                monsterSearchText = ""
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        fontSize = 16.sp,
                                        color = Color(0xFF222222)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedMonsterRequirements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    selectedMonsterRequirements.forEachIndexed { index, requirement ->
                        val selectedMonster = commissionMonsterByVariableName[requirement.monsterVariableName]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = selectedMonster?.displayName.orEmpty(),
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF222222)
                            )
                            listOf(3, 4).forEach { count ->
                                Button(
                                    onClick = {
                                        selectedMonsterRequirements[index] = requirement.copy(
                                            requiredCount = count
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (requirement.requiredCount == count) {
                                            Color(0xFF6D4BB8)
                                        } else {
                                            Color(0xFFE8E2F4)
                                        },
                                        contentColor = if (requirement.requiredCount == count) {
                                            Color.White
                                        } else {
                                            Color(0xFF5B3E9E)
                                        }
                                    )
                                ) {
                                    Text("x$count")
                                }
                            }
                            TextButton(
                                onClick = {
                                    selectedMonsterRequirements.removeAt(index)
                                }
                            ) {
                                Text("删除")
                            }
                        }

                        if (index != selectedMonsterRequirements.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            shouldShowResults = true
                            expandedDungeonVariableName = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("搜索副本")
                    }
                }
            }
        }

        if (shouldShowResults) {
            Spacer(modifier = Modifier.height(16.dp))

            if (routeResults.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "未找到同时包含已选怪物的副本",
                        modifier = Modifier.padding(18.dp),
                        fontSize = 16.sp,
                        color = Color(0xFF777777)
                    )
                }
            } else {
                routeResults.forEach { routeResult ->
                    CommissionDungeonMapCard(
                        dungeonMap = routeResult.dungeonMap,
                        routePlan = routeResult.routePlan,
                        requirements = routeResult.requirements,
                        expanded = expandedDungeonVariableName == routeResult.routeKey,
                        onToggleExpanded = {
                            expandedDungeonVariableName = if (expandedDungeonVariableName == routeResult.routeKey) {
                                ""
                            } else {
                                routeResult.routeKey
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "功能思路参考杖剑工具小程序同类功能，副本数据来源李沐泽，界面与路线算法由本软件独立开发",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SearchFieldIcon() {
    Canvas(modifier = Modifier.size(22.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            color = Color(0xFF777777),
            radius = size.minDimension * 0.32f,
            center = Offset(size.width * 0.42f, size.height * 0.42f),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = Color(0xFF777777),
            start = Offset(size.width * 0.64f, size.height * 0.64f),
            end = Offset(size.width * 0.86f, size.height * 0.86f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CommissionDungeonMapCard(
    dungeonMap: CommissionDungeonMap,
    routePlan: CommissionDungeonRoutePlan,
    requirements: List<CommissionMonsterRequirement>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpanded),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = dungeonMap.commissionDisplayName(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "可完成：${requirements.joinToString("、") { requirement ->
                        val displayName = commissionMonsterByVariableName[requirement.monsterVariableName]
                            ?.displayName
                            ?: requirement.monsterVariableName
                        val matchedCount = routePlan.matchedCountByMonster[requirement.monsterVariableName]
                            ?: 0
                        "${displayName}x${matchedCount}"
                    }}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "点击${if (expanded) "折叠" else "展开"}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.End
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))

                CommissionDungeonRouteCanvas(
                    dungeonMap = dungeonMap,
                    highlightedNodePath = routePlan.nodePath,
                    highlightedTargetNodeIdsByMonster = routePlan.targetNodeIdsByMonster,
                    targetMonsterVariableNames = requirements.map { it.monsterVariableName }.toSet()
                )
            }
        }
    }
}

@Composable
fun CommissionDungeonRouteCanvas(
    dungeonMap: CommissionDungeonMap,
    highlightedNodePath: List<String>,
    highlightedTargetNodeIdsByMonster: Map<String, Set<String>>,
    targetMonsterVariableNames: Set<String>
) {
    val nodeMap = remember(dungeonMap) {
        dungeonMap.nodes.associateBy { it.id }
    }
    val highlightedNodeIds = remember(highlightedNodePath) {
        highlightedNodePath.toSet()
    }
    val highlightedRouteKeys = remember(highlightedNodePath) {
        highlightedNodePath.zipWithNext()
            .map { (fromId, toId) -> commissionDungeonRouteKey(fromId, toId) }
            .toSet()
    }
    val maxRow = remember(dungeonMap) {
        dungeonMap.nodes.maxOfOrNull { it.row } ?: 0
    }
    val minColumnSlot = remember(dungeonMap) {
        dungeonMap.nodes.minOfOrNull { it.columnSlot } ?: 1
    }
    val maxColumnSlot = remember(dungeonMap) {
        dungeonMap.nodes.maxOfOrNull { it.columnSlot } ?: 5
    }
    val columnCount = (maxColumnSlot - minColumnSlot + 1).coerceAtLeast(1)
    val rowCount = maxRow + 1

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF242724))
    ) {
        val mapWidth = maxWidth
        val mapScale = (mapWidth / (88.dp * (columnCount + 1))).coerceAtMost(1f)
        val cellSize = mapWidth / (columnCount + 1)
        val mapHeight = cellSize * (rowCount + 1)

        BoxWithConstraints(
            modifier = Modifier
                .width(mapWidth)
                .height(mapHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                fun pointOf(node: CommissionDungeonNode): Offset {
                    val columnSpacing = size.width / (columnCount + 1f)
                    val rowSpacing = size.height / (rowCount + 1f)
                    return Offset(
                        x = columnSpacing * (node.columnSlot - minColumnSlot + 1),
                        y = rowSpacing * (node.row + 1)
                    )
                }

                dungeonMap.routes.forEach { route ->
                    val from = nodeMap[route.fromId] ?: return@forEach
                    val to = nodeMap[route.toId] ?: return@forEach
                    val routeKey = commissionDungeonRouteKey(route.fromId, route.toId)
                    drawLine(
                        color = if (routeKey in highlightedRouteKeys) {
                            Color(0xFFE6E86D)
                        } else {
                            Color(0xFF4A4F52)
                        },
                        start = pointOf(from),
                        end = pointOf(to),
                        strokeWidth = 8f * mapScale,
                        cap = StrokeCap.Round
                    )
                }
            }

            val nodeSize = 72.dp * mapScale
            val iconSize = 42.dp * mapScale
            val labelXOffset = 44.dp * mapScale
            val labelYOffset = 32.dp * mapScale
            val labelFontSize = (18f * mapScale).coerceAtLeast(12f).sp

            dungeonMap.nodes.forEach { node ->
                val x = maxWidth * (node.columnSlot - minColumnSlot + 1) / (columnCount + 1)
                val y = maxHeight * (node.row + 1) / (rowCount + 1f)
                Box(
                    modifier = Modifier
                        .size(nodeSize)
                        .offset(
                            x = x - nodeSize / 2,
                            y = y - nodeSize / 2
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (node.id in highlightedNodeIds) {
                                R.drawable.dailymission_map_node_chosen
                            } else {
                                R.drawable.dailymission_map_node
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                    Image(
                        painter = painterResource(id = commissionDungeonNodeIcon(node.type)),
                        contentDescription = node.label,
                        modifier = Modifier.size(iconSize)
                    )
                }

                val targetLabel = targetMonsterVariableNames
                    .filter { monsterVariableName ->
                        node.id in highlightedTargetNodeIdsByMonster[monsterVariableName].orEmpty()
                    }
                    .mapNotNull { monsterVariableName ->
                        node.monsterSpawns
                            .firstOrNull { it.monsterVariableName == monsterVariableName }
                            ?.let { spawn ->
                                val displayName = commissionMonsterByVariableName[monsterVariableName]
                                    ?.displayName
                                    ?: monsterVariableName
                                "${displayName}x${spawn.count}"
                            }
                    }
                    .joinToString("、")

                if (targetLabel.isNotBlank()) {
                    Text(
                        text = targetLabel,
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6E86D),
                        modifier = Modifier
                            .offset(
                                x = x - labelXOffset,
                                y = y + labelYOffset
                            )
                    )
                }
            }
        }
    }
}

fun CommissionDungeonMap.planRouteForMonsters(
    requirements: List<CommissionMonsterRequirement>
): CommissionDungeonRoutePlan? {
    if (requirements.isEmpty()) return null
    val startNode = nodes
        .filter { it.row == nodes.maxOfOrNull { node -> node.row } }
        .minByOrNull { abs(it.columnSlot - 3) }
        ?: return null
    val destinationNode = destinationNode() ?: return null
    val completeRoutes = routeNodeIdPathsBetween(startNode.id, destinationNode.id)
    if (completeRoutes.isEmpty()) return null

    data class Candidate(
        val path: List<String>,
        val targetNodeIdsByMonster: Map<String, Set<String>>,
        val matchedCountByMonster: Map<String, Int>,
        val allRequiredCountsMet: Boolean
    )

    val candidates = completeRoutes.mapNotNull { path ->
        val targetNodeIdsByMonster = requirements.associate { requirement ->
            requirement.monsterVariableName to path.filter { nodeId ->
                nodeById(nodeId)?.monsterSpawns
                    ?.any { spawn -> spawn.monsterVariableName == requirement.monsterVariableName } == true
            }.toSet()
        }
        if (targetNodeIdsByMonster.values.any { it.isEmpty() }) return@mapNotNull null

        val matchedCountByMonster = requirements.associate { requirement ->
            requirement.monsterVariableName to path.sumOf { nodeId ->
                nodeById(nodeId)?.monsterSpawns
                    ?.firstOrNull { spawn -> spawn.monsterVariableName == requirement.monsterVariableName }
                    ?.count
                    ?: 0
            }
        }
        Candidate(
            path = path,
            targetNodeIdsByMonster = targetNodeIdsByMonster,
            matchedCountByMonster = matchedCountByMonster,
            allRequiredCountsMet = requirements.all { requirement ->
                matchedCountByMonster.getValue(requirement.monsterVariableName) >= requirement.requiredCount
            }
        )
    }

    val bestCandidate = candidates.minWithOrNull(
        compareByDescending<Candidate> { it.allRequiredCountsMet }
            .thenByDescending { campNodeCount(it.path) }
            .thenBy { it.path.size }
            .thenByDescending { candidate ->
                candidate.targetNodeIdsByMonster.values
                    .flatten()
                    .mapNotNull { nodeById(it)?.row }
                    .maxOrNull()
                    ?: 0
            }
            .thenBy { candidate ->
                candidate.targetNodeIdsByMonster.values
                    .flatten()
                    .mapNotNull { nodeById(it)?.columnSlot }
                    .minOrNull()
                    ?: Int.MAX_VALUE
            }
    ) ?: return null

    return CommissionDungeonRoutePlan(
        nodePath = bestCandidate.path,
        targetNodeIdsByMonster = bestCandidate.targetNodeIdsByMonster,
        matchedCountByMonster = bestCandidate.matchedCountByMonster
    )
}

fun CommissionDungeonMap.planRouteGroupsForMonsters(
    requirements: List<CommissionMonsterRequirement>
): List<Pair<CommissionDungeonRoutePlan, List<CommissionMonsterRequirement>>> {
    if (requirements.isEmpty()) return emptyList()
    val startNode = nodes
        .filter { it.row == nodes.maxOfOrNull { node -> node.row } }
        .minByOrNull { abs(it.columnSlot - 3) }
        ?: return emptyList()
    val destinationNode = destinationNode() ?: return emptyList()
    val completeRoutes = routeNodeIdPathsBetween(startNode.id, destinationNode.id)
    if (completeRoutes.isEmpty()) return emptyList()

    data class RouteCoverage(
        val routePlan: CommissionDungeonRoutePlan,
        val coveredRequirements: Set<CommissionMonsterRequirement>
    )

    val routeCoverages = completeRoutes.mapNotNull { path ->
        val coveredRequirements = requirements.filter { requirement ->
            countMonsterOnPath(path, requirement.monsterVariableName) >= requirement.requiredCount
        }.toSet()
        if (coveredRequirements.isEmpty()) return@mapNotNull null

        val targetNodeIdsByMonster = coveredRequirements.associate { requirement ->
            requirement.monsterVariableName to path.filter { nodeId ->
                nodeById(nodeId)?.monsterSpawns
                    ?.any { spawn -> spawn.monsterVariableName == requirement.monsterVariableName } == true
            }.toSet()
        }
        val matchedCountByMonster = coveredRequirements.associate { requirement ->
            requirement.monsterVariableName to countMonsterOnPath(path, requirement.monsterVariableName)
        }

        RouteCoverage(
            routePlan = CommissionDungeonRoutePlan(
                nodePath = path,
                targetNodeIdsByMonster = targetNodeIdsByMonster,
                matchedCountByMonster = matchedCountByMonster
            ),
            coveredRequirements = coveredRequirements
        )
    }.sortedWith(
        compareByDescending<RouteCoverage> { it.coveredRequirements.size }
            .thenByDescending { campNodeCount(it.routePlan.nodePath) }
            .thenBy { routeCoverage ->
                routeCoverage.coveredRequirements.sumOf { requirement -> requirements.indexOf(requirement) }
            }
            .thenBy { it.routePlan.nodePath.size }
    )
    if (routeCoverages.isEmpty()) return emptyList()

    val allRequirements = requirements.toSet()
    var bestCombination: List<RouteCoverage>? = null

    fun betterCombination(
        candidate: List<RouteCoverage>,
        currentBest: List<RouteCoverage>?
    ): Boolean {
        if (currentBest == null) return true
        if (candidate.size != currentBest.size) return candidate.size < currentBest.size
        val candidateCampCount = candidate.sumOf { campNodeCount(it.routePlan.nodePath) }
        val bestCampCount = currentBest.sumOf { campNodeCount(it.routePlan.nodePath) }
        if (candidateCampCount != bestCampCount) return candidateCampCount > bestCampCount
        val candidateLength = candidate.sumOf { it.routePlan.nodePath.size }
        val bestLength = currentBest.sumOf { it.routePlan.nodePath.size }
        return candidateLength < bestLength
    }

    fun searchCombination(
        startIndex: Int,
        selectedRoutes: List<RouteCoverage>,
        coveredRequirements: Set<CommissionMonsterRequirement>
    ) {
        if (coveredRequirements.containsAll(allRequirements)) {
            if (betterCombination(selectedRoutes, bestCombination)) {
                bestCombination = selectedRoutes
            }
            return
        }
        if (startIndex >= routeCoverages.size) return
        if (bestCombination != null && selectedRoutes.size >= bestCombination.orEmpty().size) return

        for (index in startIndex until routeCoverages.size) {
            val routeCoverage = routeCoverages[index]
            val newRequirements = routeCoverage.coveredRequirements - coveredRequirements
            if (newRequirements.isEmpty()) continue
            searchCombination(
                startIndex = index + 1,
                selectedRoutes = selectedRoutes + routeCoverage,
                coveredRequirements = coveredRequirements + routeCoverage.coveredRequirements
            )
        }
    }

    searchCombination(
        startIndex = 0,
        selectedRoutes = emptyList(),
        coveredRequirements = emptySet()
    )

    return bestCombination.orEmpty().fold(
        emptyList<Pair<CommissionDungeonRoutePlan, List<CommissionMonsterRequirement>>>() to emptySet<CommissionMonsterRequirement>()
    ) { (routeGroups, alreadyAssignedRequirements), routeCoverage ->
        val routeRequirements = (routeCoverage.coveredRequirements - alreadyAssignedRequirements)
            .sortedBy { requirement -> requirements.indexOf(requirement) }
        if (routeRequirements.isEmpty()) {
            routeGroups to alreadyAssignedRequirements
        } else {
            routeGroups + (routeCoverage.routePlan to routeRequirements) to
                (alreadyAssignedRequirements + routeRequirements)
        }
    }.first
}

fun CommissionDungeonMap.planRouteForMonster(
    monsterVariableName: String,
    requiredCount: Int
): CommissionDungeonRoutePlan? {
    return planRouteForMonsters(
        listOf(
            CommissionMonsterRequirement(
                monsterVariableName = monsterVariableName,
                requiredCount = requiredCount
            )
        )
    )
}

fun CommissionDungeonMap.countMonsterOnPath(path: List<String>, monsterVariableName: String): Int {
    return path.sumOf { nodeId ->
        nodeById(nodeId)?.monsterSpawns
            ?.firstOrNull { spawn -> spawn.monsterVariableName == monsterVariableName }
            ?.count
            ?: 0
    }
}

fun commissionMonsterVariableName(regionCode: String, monsterCode: String): String {
    return "dailymisson_monsterlist_${regionCode}_$monsterCode"
}

fun CommissionDungeonMap.commissionDisplayName(): String {
    val parts = variableName.split("_")
    if (parts.size < 4) return name
    val regionName = commissionDungeonRegionNameByCode[parts[1]] ?: parts[1]
    val instanceName = commissionDungeonInstanceNameByCode[parts[2]] ?: parts[2]
    val difficultyName = commissionDungeonDifficultyNameBySuffix[parts[3]] ?: parts[3]
    return "$regionName-$instanceName-$difficultyName"
}

fun CommissionDungeonMap.destinationNode(): CommissionDungeonNode? {
    return nodes
        .filter { it.type == CommissionDungeonNodeType.Destination }
        .minWithOrNull(compareBy<CommissionDungeonNode> { it.row }.thenBy { it.columnSlot })
        ?: nodes.minWithOrNull(compareBy<CommissionDungeonNode> { it.row }.thenBy { it.columnSlot })
}

fun CommissionDungeonMap.routeNodeIdPathsBetween(startId: String, targetId: String): List<List<String>> {
    val adjacency = buildMap<String, MutableList<String>> {
        routes.forEach { route ->
            getOrPut(route.fromId) { mutableListOf() }.add(route.toId)
            getOrPut(route.toId) { mutableListOf() }.add(route.fromId)
        }
    }
    val candidatePaths = mutableListOf<List<String>>()

    fun dfs(currentId: String, path: List<String>) {
        if (currentId == targetId) {
            candidatePaths += path
            return
        }
        val currentNode = nodeById(currentId) ?: return
        adjacency[currentId].orEmpty()
            .mapNotNull { nodeId -> nodeById(nodeId) }
            .filter { nextNode -> nextNode.row < currentNode.row }
            .sortedWith(compareByDescending<CommissionDungeonNode> { it.row }.thenBy { it.columnSlot })
            .forEach { nextNode ->
                if (nextNode.id !in path) {
                    dfs(nextNode.id, path + nextNode.id)
                }
            }
    }

    dfs(startId, listOf(startId))
    return candidatePaths
}

fun CommissionDungeonMap.extendRouteToDestination(path: List<String>): List<String> {
    if (path.isEmpty()) return emptyList()
    val destinationNode = nodes
        .filter { it.type == CommissionDungeonNodeType.Destination }
        .minWithOrNull(compareBy<CommissionDungeonNode> { it.row }.thenBy { it.columnSlot })
        ?: nodes.minWithOrNull(compareBy<CommissionDungeonNode> { it.row }.thenBy { it.columnSlot })
        ?: return path
    if (path.last() == destinationNode.id) return path
    val tail = routeNodeIdsBetween(path.last(), destinationNode.id) ?: return path
    return path + tail.drop(1)
}

fun CommissionDungeonMap.routeNodeIdsBetween(startId: String, targetId: String): List<String>? {
    val adjacency = buildMap<String, MutableList<String>> {
        routes.forEach { route ->
            getOrPut(route.fromId) { mutableListOf() }.add(route.toId)
            getOrPut(route.toId) { mutableListOf() }.add(route.fromId)
        }
    }
    val candidatePaths = mutableListOf<List<String>>()

    fun dfs(currentId: String, path: List<String>) {
        if (currentId == targetId) {
            candidatePaths += path
            return
        }
        val currentNode = nodeById(currentId) ?: return
        adjacency[currentId].orEmpty()
            .mapNotNull { nodeId -> nodeById(nodeId) }
            .filter { nextNode -> nextNode.row < currentNode.row }
            .sortedWith(compareByDescending<CommissionDungeonNode> { it.row }.thenBy { it.columnSlot })
            .forEach { nextNode ->
                if (nextNode.id !in path) {
                    dfs(nextNode.id, path + nextNode.id)
                }
            }
    }

    dfs(startId, listOf(startId))

    return candidatePaths.minWithOrNull(
        compareByDescending<List<String>> { campNodeCount(it) }
            .thenBy { path -> path.size }
            .thenByDescending { path -> path.maxOfOrNull { nodeById(it)?.row ?: 0 } ?: 0 }
            .thenBy { path -> path.mapNotNull { nodeById(it)?.columnSlot }.minOrNull() ?: Int.MAX_VALUE }
    )
}

fun CommissionDungeonMap.findCombinedRouteNodeIds(
    startId: String,
    monsterVariableName: String,
    requiredCount: Int
): List<String>? {
    val adjacency = buildMap<String, MutableList<String>> {
        routes.forEach { route ->
            getOrPut(route.fromId) { mutableListOf() }.add(route.toId)
            getOrPut(route.toId) { mutableListOf() }.add(route.fromId)
        }
    }
    val validPaths = mutableListOf<List<String>>()

    fun matchedCount(path: List<String>): Int {
        return path.sumOf { nodeId ->
            nodeById(nodeId)?.monsterSpawns
                ?.firstOrNull { spawn -> spawn.monsterVariableName == monsterVariableName }
                ?.count
                ?: 0
        }
    }

    fun dfs(currentId: String, path: List<String>) {
        if (matchedCount(path) >= requiredCount) {
            validPaths += path
            return
        }
        val currentNode = nodeById(currentId) ?: return
        adjacency[currentId].orEmpty()
            .mapNotNull { nodeId -> nodeById(nodeId) }
            .filter { nextNode -> nextNode.id !in path }
            .filter { nextNode -> nextNode.row < currentNode.row }
            .sortedWith(compareByDescending<CommissionDungeonNode> { it.row }.thenBy { it.columnSlot })
            .forEach { nextNode ->
                dfs(nextNode.id, path + nextNode.id)
            }
    }

    dfs(startId, listOf(startId))

    return validPaths
        .filter { path ->
            path.count { nodeId ->
                nodeById(nodeId)?.monsterSpawns
                    ?.any { spawn -> spawn.monsterVariableName == monsterVariableName } == true
            } >= 2
        }
        .minWithOrNull(compareByDescending<List<String>> { campNodeCount(it) }
            .thenBy { path -> path.size }
            .thenByDescending { path -> path.maxOfOrNull { nodeById(it)?.row ?: 0 } ?: 0 }
            .thenBy { path -> path.mapNotNull { nodeById(it)?.columnSlot }.minOrNull() ?: Int.MAX_VALUE })
}

fun CommissionDungeonMap.nodeById(nodeId: String): CommissionDungeonNode? {
    return nodes.firstOrNull { it.id == nodeId }
}

fun CommissionDungeonMap.campNodeCount(path: List<String>): Int {
    return path.count { nodeId -> nodeById(nodeId)?.type == CommissionDungeonNodeType.Camp }
}

fun commissionDungeonRouteKey(fromId: String, toId: String): String {
    return if (fromId <= toId) {
        "$fromId->$toId"
    } else {
        "$toId->$fromId"
    }
}

fun commissionDungeonNodeIcon(type: CommissionDungeonNodeType): Int {
    return when (type) {
        CommissionDungeonNodeType.Normal -> R.drawable.dailymission_map_camp
        CommissionDungeonNodeType.Creep -> R.drawable.dailymission_map_monster_creep
        CommissionDungeonNodeType.Elite -> R.drawable.dailymission_map_monster_elite
        CommissionDungeonNodeType.Boss -> R.drawable.dailymission_map_monster_boss
        CommissionDungeonNodeType.Camp -> R.drawable.dailymission_map_camp
        CommissionDungeonNodeType.Destination -> R.drawable.dailymission_map_destination
    }
}

@Composable
fun AssaultArmorBreakCalculator() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    }
    var attackText by remember {
        mutableStateOf(prefs.getString(ASSAULT_ARMOR_BREAK_ATTACK_KEY, "").orEmpty())
    }
    var defenseText by remember {
        mutableStateOf(prefs.getString(ASSAULT_ARMOR_BREAK_DEFENSE_KEY, "").orEmpty())
    }
    var armorBreakFlatText by remember {
        mutableStateOf(prefs.getString(ASSAULT_ARMOR_BREAK_FLAT_KEY, "").orEmpty())
    }
    var armorBreakOptionIndex by remember {
        mutableIntStateOf(
            prefs.getInt(ASSAULT_ARMOR_BREAK_OPTION_INDEX_KEY, 0)
                .coerceIn(assaultArmorBreakOptions.indices)
        )
    }
    val armorBreakOption = assaultArmorBreakOptions[armorBreakOptionIndex]
    val onArmorBreakOptionClick = {
        armorBreakOptionIndex = (armorBreakOptionIndex + 1) % assaultArmorBreakOptions.size
        prefs.edit {
            putInt(ASSAULT_ARMOR_BREAK_OPTION_INDEX_KEY, armorBreakOptionIndex)
        }
    }

    val result = calculateAssaultArmorBreakResult(
        attack = attackText.toDoubleOrNull(),
        defense = defenseText.toDoubleOrNull(),
        armorBreakPercent = armorBreakOption.percent,
        armorBreakFlat = armorBreakFlatText.toDoubleOrNull()
    )

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
            CalculatorNumberField("玩家攻击力", attackText, {
                attackText = it
                prefs.edit { putString(ASSAULT_ARMOR_BREAK_ATTACK_KEY, it) }
            })

            Spacer(modifier = Modifier.height(12.dp))

            CalculatorNumberField("怪物防御力", defenseText, {
                defenseText = it
                prefs.edit { putString(ASSAULT_ARMOR_BREAK_DEFENSE_KEY, it) }
            })

            Spacer(modifier = Modifier.height(12.dp))

            AssaultArmorBreakPercentSelector(
                option = armorBreakOption,
                onClick = onArmorBreakOptionClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            CalculatorNumberField("强袭破甲固定值", armorBreakFlatText, {
                armorBreakFlatText = it
                prefs.edit { putString(ASSAULT_ARMOR_BREAK_FLAT_KEY, it) }
            })

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "计算结果",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ResultLine("基础攻防伤害系数", result?.baseDamageCoefficient?.let(::formatPercent) ?: "--")
            ResultLine("0层buff攻防系数", result?.zeroBuffCoefficient?.let(::formatPercent) ?: "--")
            ResultLine("0层buff等效增伤", result?.zeroBuffDamageIncrease?.let(::formatPercent) ?: "--")
            ResultLine("3层buff攻防系数", result?.threeBuffCoefficient?.let(::formatPercent) ?: "--")
            ResultLine("3层buff等效增伤", result?.threeBuffDamageIncrease?.let(::formatPercent) ?: "--")
        }
    }
}

@Composable
fun AssaultArmorBreakPercentSelector(
    option: AssaultArmorBreakOption,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = "强袭破甲百分比",
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(option.iconRes),
                contentDescription = option.quality,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                AssaultArmorBreakOptionBox(
                    text = option.quality,
                    onClick = onClick
                )

                Spacer(modifier = Modifier.height(10.dp))

                AssaultArmorBreakOptionBox(
                    text = option.percentText,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun AssaultArmorBreakOptionBox(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFB9C0CC), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )
    }
}

fun calculateAssaultArmorBreakResult(
    attack: Double?,
    defense: Double?,
    armorBreakPercent: Double?,
    armorBreakFlat: Double?
): AssaultArmorBreakResult? {
    if (
        attack == null ||
        defense == null ||
        armorBreakPercent == null ||
        armorBreakFlat == null ||
        attack <= 0.0 ||
        defense < 0.0
    ) {
        return null
    }

    val baseDamageCoefficient = attack / (attack + defense)
    val zeroBuffDefense = maxOf(0.0, defense * (1.0 - armorBreakPercent) - armorBreakFlat)
    val zeroBuffCoefficient = attack / (attack + zeroBuffDefense)
    val threeBuffDefense = maxOf(0.0, defense * (1.0 - armorBreakPercent * 1.3) - armorBreakFlat * 1.3)
    val threeBuffCoefficient = attack / (attack + threeBuffDefense)

    return AssaultArmorBreakResult(
        baseDamageCoefficient = baseDamageCoefficient,
        zeroBuffCoefficient = zeroBuffCoefficient,
        zeroBuffDamageIncrease = zeroBuffCoefficient / baseDamageCoefficient - 1.0,
        threeBuffCoefficient = threeBuffCoefficient,
        threeBuffDamageIncrease = threeBuffCoefficient / baseDamageCoefficient - 1.0
    )
}

@Composable
fun AstralKamiScreen(
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "星间之神好感度",
        subtitle = "计算星间之神最终等级",
        onBack = onBack,
        pinnedTitleBar = true
    ) {
        AstralKamiCalculator()
    }
}

@Composable
fun AstralKamiCalculator() {
    var blueGiftText by remember { mutableStateOf("") }
    var purpleGiftText by remember { mutableStateOf("") }
    var orangeGiftText by remember { mutableStateOf("") }
    var currentLevelText by remember { mutableStateOf("") }
    var currentExpText by remember { mutableStateOf("") }
    var isLevel100Active by remember { mutableStateOf(false) }
    var currentAstrolabeText by remember { mutableStateOf("") }
    var levelCorrectionText by remember { mutableStateOf<String?>(null) }
    var expCorrectionText by remember { mutableStateOf<String?>(null) }

    val result = calculateAstralKamiResult(
        blueGifts = blueGiftText.toIntOrNull() ?: 0,
        purpleGifts = purpleGiftText.toIntOrNull() ?: 0,
        orangeGifts = orangeGiftText.toIntOrNull() ?: 0,
        currentLevel = currentLevelText.toIntOrNull() ?: 1,
        currentExp = currentExpText.toIntOrNull() ?: 0,
        isLevel100Active = isLevel100Active,
        currentAstrolabe = currentAstrolabeText.toIntOrNull() ?: 0
    )
    val currentLevelValue = currentLevelText.toIntOrNull()
    val currentLevelForExpRange = currentLevelValue?.coerceIn(1, 100) ?: 1
    val maxCurrentExp = astralKamiMaxCurrentExp(currentLevelForExpRange)
    val levelErrorText = levelCorrectionText ?: validationRangeError(
        valueText = currentLevelText,
        min = 1,
        max = 100
    )
    val expErrorText = expCorrectionText ?: validationRangeError(
        valueText = currentExpText,
        min = 0,
        max = maxCurrentExp
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "已囤积礼物数量",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CalculatorNumberField(
                label = "蓝礼物数量",
                value = blueGiftText,
                onValueChange = { blueGiftText = it },
                placeholderText = "请输入数量"
            )
            Spacer(modifier = Modifier.height(8.dp))
            CalculatorNumberField(
                label = "紫礼物数量",
                value = purpleGiftText,
                onValueChange = { purpleGiftText = it },
                placeholderText = "请输入数量"
            )
            Spacer(modifier = Modifier.height(8.dp))
            CalculatorNumberField(
                label = "橙礼物数量",
                value = orangeGiftText,
                onValueChange = { orangeGiftText = it },
                placeholderText = "请输入数量"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "星间之神当前等级",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { isLevel100Active = !isLevel100Active },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLevel100Active) {
                            Color(0xFFC8E6C9)
                        } else {
                            Color(0xFFE0E0E0)
                        },
                        contentColor = Color(0xFF222222)
                    )
                ) {
                    Text(if (isLevel100Active) "星间之神已满100级" else "星间之神未满100级")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ValidatedNumberField(
                    label = "当前等级",
                    value = if (isLevel100Active) "100" else currentLevelText,
                    onValueChange = { input ->
                        val normalizedLevel = clampNumberTextToRange(
                            valueText = input,
                            min = 1,
                            max = 100
                        )
                        currentLevelText = normalizedLevel.valueText
                        levelCorrectionText = normalizedLevel.errorText

                        val nextLevel = currentLevelText.toIntOrNull()?.coerceIn(1, 100) ?: 1
                        val nextMaxExp = astralKamiMaxCurrentExp(nextLevel)
                        val normalizedExp = clampNumberTextToRange(
                            valueText = currentExpText,
                            min = 0,
                            max = nextMaxExp
                        )
                        currentExpText = normalizedExp.valueText
                        expCorrectionText = normalizedExp.errorText
                    },
                    modifier = Modifier.weight(1f),
                    placeholderText = "1",
                    errorText = if (isLevel100Active) null else levelErrorText,
                    enabled = !isLevel100Active
                )

                Spacer(modifier = Modifier.width(12.dp))

                ValidatedNumberField(
                    label = "当前经验",
                    value = if (isLevel100Active) "0" else currentExpText,
                    onValueChange = { input ->
                        val normalizedExp = clampNumberTextToRange(
                            valueText = input,
                            min = 0,
                            max = maxCurrentExp
                        )
                        currentExpText = normalizedExp.valueText
                        expCorrectionText = normalizedExp.errorText
                    },
                    modifier = Modifier.weight(1f),
                    placeholderText = "0",
                    errorText = if (isLevel100Active) null else expErrorText,
                    enabled = !isLevel100Active
                )
            }

            if (isLevel100Active) {
                Spacer(modifier = Modifier.height(8.dp))
                CalculatorNumberField(
                    label = "已有神权星盘数量",
                    value = currentAstrolabeText,
                    onValueChange = { input ->
                        currentAstrolabeText = clampNumberTextToRange(input, 0, 132).valueText
                    },
                    placeholderText = "0"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ResultLine("已囤积礼物经验", result.giftExp.toString())
                    if (!isLevel100Active) {
                        ResultLine("已累计的经验进度", String.format(Locale.US, "%.2f%%", result.progressPercent))
                        ResultLine("星间之神最终等级", "${result.finalLevel}级")
                    }
                    ResultLine("神权星盘", "${result.divineAstrolabe}/132")
                    result.overflowGiftText?.let {
                        ResultLine("已溢出礼物数量", it)
                    }
                }
            }
        }
    }
}

fun calculateAstralKamiResult(
    blueGifts: Int,
    purpleGifts: Int,
    orangeGifts: Int,
    currentLevel: Int,
    currentExp: Int,
    isLevel100Active: Boolean = false,
    currentAstrolabe: Int = 0
): AstralKamiResult {
    val safeBlueGifts = blueGifts.coerceAtLeast(0)
    val safePurpleGifts = purpleGifts.coerceAtLeast(0)
    val safeOrangeGifts = orangeGifts.coerceAtLeast(0)
    val giftExp = safeBlueGifts.toLong() * ASTRAL_BLUE_GIFT_EXP +
            safePurpleGifts.toLong() * ASTRAL_PURPLE_GIFT_EXP +
            safeOrangeGifts.toLong() * ASTRAL_ORANGE_GIFT_EXP
    if (isLevel100Active) {
        val extraAstrolabe = giftExp / ASTRAL_DIVINE_ASTROLABE_EXP
        val rawAstrolabe = currentAstrolabe.coerceAtLeast(0).toLong() + extraAstrolabe
        val overflowAstrolabe = (rawAstrolabe - ASTRAL_DIVINE_ASTROLABE_LIMIT).coerceAtLeast(0L)
        return AstralKamiResult(
            giftExp = giftExp,
            progressPercent = 100.0,
            finalLevel = 100,
            divineAstrolabe = rawAstrolabe.coerceAtMost(ASTRAL_DIVINE_ASTROLABE_LIMIT.toLong()).toInt(),
            overflowGiftText = astralKamiOverflowGiftText(
                overflowExp = overflowAstrolabe * ASTRAL_DIVINE_ASTROLABE_EXP,
                blueGifts = safeBlueGifts,
                purpleGifts = safePurpleGifts,
                orangeGifts = safeOrangeGifts
            )
        )
    }

    val giftExpBeforeLevel100 = astralKamiPreLevel100GiftExp(
        blueGifts = safeBlueGifts,
        purpleGifts = safePurpleGifts,
        orangeGifts = safeOrangeGifts
    )
    val currentTotalExp = currentAstralKamiTotalExp(
        currentLevel = currentLevel,
        currentExp = currentExp
    )
    val currentExtraAfterLevel100 = (currentTotalExp - astralKamiTotalExpTo100).coerceAtLeast(0)
    val currentExpBeforeLevel100 = currentTotalExp.coerceIn(0, astralKamiTotalExpTo100)
    val expNeededToLevel100 = astralKamiTotalExpTo100 - currentExpBeforeLevel100
    val usedPreLevel100GiftExp = if (expNeededToLevel100 <= 0) {
        0
    } else {
        astralKamiMinPreLevel100GiftExpToReach(
            requiredExp = expNeededToLevel100,
            blueGifts = safeBlueGifts,
            purpleGifts = safePurpleGifts,
            orangeGifts = safeOrangeGifts
        ) ?: giftExpBeforeLevel100
    }
    val usedBaseGiftExp = astralKamiBaseGiftExpFromPreLevel100Exp(usedPreLevel100GiftExp).toLong()
    val extraGiftExpAfterLevel100 = if (currentExpBeforeLevel100 + giftExpBeforeLevel100 >= astralKamiTotalExpTo100) {
        (giftExp - usedBaseGiftExp).coerceAtLeast(0)
    } else {
        0L
    }
    val cappedTotalExp = (currentExpBeforeLevel100 + giftExpBeforeLevel100)
        .coerceIn(0, astralKamiTotalExpTo100)
    val finalLevel = astralKamiFinalLevel(cappedTotalExp)
    val levelAstrolabe = astralKamiLevelAstrolabe(finalLevel)
    val extraAstrolabe = (currentExtraAfterLevel100.toLong() + extraGiftExpAfterLevel100) / ASTRAL_DIVINE_ASTROLABE_EXP
    val rawAstrolabe = levelAstrolabe.toLong() + extraAstrolabe
    val overflowAstrolabe = (rawAstrolabe - ASTRAL_DIVINE_ASTROLABE_LIMIT).coerceAtLeast(0L)
    return AstralKamiResult(
        giftExp = giftExp,
        progressPercent = cappedTotalExp * 100.0 / astralKamiTotalExpTo100,
        finalLevel = finalLevel,
        divineAstrolabe = rawAstrolabe.coerceAtMost(ASTRAL_DIVINE_ASTROLABE_LIMIT.toLong()).toInt(),
        overflowGiftText = astralKamiOverflowGiftText(
            overflowExp = overflowAstrolabe * ASTRAL_DIVINE_ASTROLABE_EXP,
            blueGifts = safeBlueGifts,
            purpleGifts = safePurpleGifts,
            orangeGifts = safeOrangeGifts
        )
    )
}

fun astralKamiPreLevel100GiftExp(
    blueGifts: Int,
    purpleGifts: Int,
    orangeGifts: Int
): Int {
    val preLevel100Exp = (blueGifts.coerceAtLeast(0).toLong() * ASTRAL_BLUE_GIFT_EXP +
            purpleGifts.coerceAtLeast(0).toLong() * ASTRAL_PURPLE_GIFT_EXP +
            orangeGifts.coerceAtLeast(0).toLong() * ASTRAL_ORANGE_GIFT_EXP) *
            ASTRAL_PRE_LEVEL_100_BONUS_NUMERATOR / ASTRAL_PRE_LEVEL_100_BONUS_DENOMINATOR
    return preLevel100Exp.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

fun astralKamiBaseGiftExpFromPreLevel100Exp(preLevel100Exp: Int): Int {
    return preLevel100Exp * ASTRAL_PRE_LEVEL_100_BONUS_DENOMINATOR /
            ASTRAL_PRE_LEVEL_100_BONUS_NUMERATOR
}

fun astralKamiMinPreLevel100GiftExpToReach(
    requiredExp: Int,
    blueGifts: Int,
    purpleGifts: Int,
    orangeGifts: Int
): Int? {
    if (requiredExp <= 0) {
        return 0
    }

    val bluePreExp = ASTRAL_BLUE_GIFT_EXP * ASTRAL_PRE_LEVEL_100_BONUS_NUMERATOR /
            ASTRAL_PRE_LEVEL_100_BONUS_DENOMINATOR
    val purplePreExp = ASTRAL_PURPLE_GIFT_EXP * ASTRAL_PRE_LEVEL_100_BONUS_NUMERATOR /
            ASTRAL_PRE_LEVEL_100_BONUS_DENOMINATOR
    val orangePreExp = ASTRAL_ORANGE_GIFT_EXP * ASTRAL_PRE_LEVEL_100_BONUS_NUMERATOR /
            ASTRAL_PRE_LEVEL_100_BONUS_DENOMINATOR
    val totalPreExp = blueGifts.coerceAtLeast(0).toLong() * bluePreExp +
            purpleGifts.coerceAtLeast(0).toLong() * purplePreExp +
            orangeGifts.coerceAtLeast(0).toLong() * orangePreExp

    if (totalPreExp < requiredExp) {
        return null
    }

    val maxUnits = (requiredExp + orangePreExp + bluePreExp - 1) / bluePreExp
    val targetUnits = (requiredExp + bluePreExp - 1) / bluePreExp
    val possible = BooleanArray(maxUnits + 1)
    possible[0] = true

    fun addGifts(count: Int, units: Int) {
        repeat(count.coerceAtMost(maxUnits / units)) {
            for (sum in maxUnits downTo units) {
                if (possible[sum - units]) {
                    possible[sum] = true
                }
            }
        }
    }

    addGifts(blueGifts, 1)
    addGifts(purpleGifts, purplePreExp / bluePreExp)
    addGifts(orangeGifts, orangePreExp / bluePreExp)

    for (units in targetUnits..maxUnits) {
        if (possible[units]) {
            return units * bluePreExp
        }
    }

    return totalPreExp.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

fun astralKamiLevelAstrolabe(finalLevel: Int): Int {
    return (10..100 step 10)
        .filter { finalLevel >= it }
        .sumOf { level ->
            if (level == 50 || level == 100) 4 else 2
        }
}

fun astralKamiOverflowGiftText(
    overflowExp: Long,
    blueGifts: Int,
    purpleGifts: Int,
    orangeGifts: Int
): String? {
    if (overflowExp <= 0L) {
        return null
    }
    if (overflowExp >= ASTRAL_OVERFLOW_GIFT_EXP_TEXT_LIMIT) {
        return "礼物溢出过多"
    }

    var remainingExp = overflowExp
    val orangeOverflow = minOf(orangeGifts.coerceAtLeast(0).toLong(), remainingExp / ASTRAL_ORANGE_GIFT_EXP)
    remainingExp -= orangeOverflow * ASTRAL_ORANGE_GIFT_EXP
    val purpleOverflow = minOf(purpleGifts.coerceAtLeast(0).toLong(), remainingExp / ASTRAL_PURPLE_GIFT_EXP)
    remainingExp -= purpleOverflow * ASTRAL_PURPLE_GIFT_EXP
    val blueOverflow = minOf(
        blueGifts.coerceAtLeast(0).toLong(),
        (remainingExp + ASTRAL_BLUE_GIFT_EXP - 1) / ASTRAL_BLUE_GIFT_EXP
    )

    return listOf(
        "橙$orangeOverflow",
        "紫$purpleOverflow",
        "蓝$blueOverflow"
    ).joinToString("，")
}

fun currentAstralKamiTotalExp(
    currentLevel: Int,
    currentExp: Int
): Int {
    val safeLevel = currentLevel.coerceIn(1, 100)
    val finishedLevelExp = astralKamiLevelExpTable
        .filter { it.first <= safeLevel }
        .sumOf { it.second }
    return finishedLevelExp + currentExp.coerceAtLeast(0)
}

fun astralKamiFinalLevel(totalExp: Int): Int {
    var level = 1
    var remainingExp = totalExp.coerceAtLeast(0)
    for ((targetLevel, requiredExp) in astralKamiLevelExpTable) {
        if (remainingExp < requiredExp) {
            break
        }
        remainingExp -= requiredExp
        level = targetLevel
    }
    return level
}

fun astralKamiMaxCurrentExp(currentLevel: Int): Int {
    val safeLevel = currentLevel.coerceIn(1, 100)
    return if (safeLevel >= 100) {
        0
    } else {
        astralKamiLevelExpTable.firstOrNull { it.first == safeLevel + 1 }?.second?.minus(1) ?: 0
    }
}

fun validationRangeError(
    valueText: String,
    min: Int,
    max: Int
): String? {
    if (valueText.isBlank()) {
        return null
    }

    val value = valueText.toIntOrNull() ?: return "输入超出范围，范围为 $min-$max"
    return if (value in min..max) {
        null
    } else {
        "输入超出范围，范围为 $min-$max"
    }
}

data class NumberRangeClampResult(
    val valueText: String,
    val errorText: String?
)

fun clampNumberTextToRange(
    valueText: String,
    min: Int,
    max: Int
): NumberRangeClampResult {
    if (valueText.isBlank()) {
        return NumberRangeClampResult("", null)
    }

    val value = valueText.toIntOrNull()
        ?: return NumberRangeClampResult(valueText, "输入超出范围，范围为 $min-$max")
    val clampedValue = value.coerceIn(min, max)
    return NumberRangeClampResult(
        valueText = clampedValue.toString(),
        errorText = if (value == clampedValue) null else "输入超出范围，范围为 $min-$max"
    )
}

data class JobTransferBonus(
    val transferCount: String,
    val jobName: String,
    val transferName: String,
    val attack: Double,
    val defense: Double,
    val health: Double,
    val speed: Double
)

data class AttributePowerCoefficient(
    val attack: Double,
    val defense: Double,
    val health: Double,
    val speed: Double
)

data class CharacterAttributePower(
    val attack: Double,
    val defense: Double,
    val health: Double,
    val speed: Double
)

data class GemUpgradeRule(
    val targetLevel: Int,
    val materialLevel: Int,
    val successRate: Double,
    val pityFailures: Int
)

data class GemPowerRow(
    val level: Int,
    val upgradeGemCount: Double,
    val consumedLevelFiveGems: Double,
    val powerPerLevelFiveGem: Double
)

private val attributePowerCoefficientTable = AttributePowerCoefficient(
    attack = 2.5,
    defense = 2.5,
    health = 0.5,
    speed = 3.125
)

private val gemUpgradeRules = listOf(
    GemUpgradeRule(targetLevel = 6, materialLevel = 5, successRate = 1.0, pityFailures = 0),
    GemUpgradeRule(targetLevel = 7, materialLevel = 6, successRate = 0.36, pityFailures = 2),
    GemUpgradeRule(targetLevel = 8, materialLevel = 7, successRate = 0.18, pityFailures = 5),
    GemUpgradeRule(targetLevel = 9, materialLevel = 8, successRate = 0.08, pityFailures = 11),
    GemUpgradeRule(targetLevel = 10, materialLevel = 9, successRate = 0.04, pityFailures = 23),
    GemUpgradeRule(targetLevel = 11, materialLevel = 10, successRate = 1.0, pityFailures = 0),
    GemUpgradeRule(targetLevel = 12, materialLevel = 10, successRate = 0.36, pityFailures = 2),
    GemUpgradeRule(targetLevel = 13, materialLevel = 10, successRate = 0.18, pityFailures = 5),
    GemUpgradeRule(targetLevel = 14, materialLevel = 10, successRate = 0.08, pityFailures = 11)
)

private val attributeTransferBonusTable = buildList {
    add(JobTransferBonus("一转", "法师", "法师", 0.14, 0.05, 0.05, 0.06))
    add(JobTransferBonus("一转", "战士", "战士", 0.06, 0.09, 0.09, 0.06))
    add(JobTransferBonus("二转", "术士", "术士", 0.19, 0.09, 0.1, 0.12))
    add(JobTransferBonus("二转", "贤者", "贤者", 0.125, 0.125, 0.125, 0.125))
    add(JobTransferBonus("二转", "斗士", "斗士", 0.15, 0.1, 0.1, 0.15))
    add(JobTransferBonus("二转", "骑士", "骑士", 0.09, 0.15, 0.15, 0.11))
    add(JobTransferBonus("三转", "术士", "魔导师", 0.295, 0.16, 0.165, 0.2))
    add(JobTransferBonus("三转", "贤者", "秘术师", 0.205, 0.205, 0.205, 0.205))
    add(JobTransferBonus("三转", "斗士", "狂战士", 0.24, 0.17, 0.17, 0.24))
    add(JobTransferBonus("三转", "骑士", "圣骑士", 0.16, 0.24, 0.24, 0.18))
    add(JobTransferBonus("四转", "术士", "毁灭者", 0.39, 0.26, 0.27, 0.3))
    add(JobTransferBonus("四转", "贤者", "掌控者", 0.305, 0.305, 0.305, 0.305))
    add(JobTransferBonus("四转", "斗士", "征服者", 0.345, 0.27, 0.265, 0.34))
    add(JobTransferBonus("四转", "骑士", "守护者", 0.26, 0.34, 0.34, 0.28))
    add(JobTransferBonus("五转", "术士", "黯月魔导", 0.54, 0.36, 0.37, 0.4))
    add(JobTransferBonus("五转", "贤者", "奥法先知", 0.405, 0.405, 0.405, 0.405))
    add(JobTransferBonus("五转", "斗士", "破晓狂刃", 0.44, 0.37, 0.37, 0.49))
    add(JobTransferBonus("五转", "骑士", "誓约圣骑", 0.36, 0.49, 0.44, 0.38))
    add(JobTransferBonus("六转", "术士", "爆裂魔导", 0.68, 0.49, 0.48, 0.52))
    add(JobTransferBonus("六转", "贤者", "真理贤王", 0.53, 0.525, 0.605, 0.51))
    add(JobTransferBonus("六转", "斗士", "瞬狱魔剑", 0.555, 0.49, 0.485, 0.64))
    add(JobTransferBonus("六转", "骑士", "苍穹圣骑", 0.48, 0.64, 0.56, 0.49))
}
private val staffJobs = listOf("法师", "术士", "贤者")
private val swordJobs = listOf("战士", "斗士", "骑士")

fun transferDisplayText(entry: JobTransferBonus): String {
    return "${entry.transferCount}\n${entry.transferName}"
}

fun calculateCharacterAttributePower(
    attackText: String,
    defenseText: String,
    healthText: String,
    speedText: String,
    selectedBonus: JobTransferBonus?
): CharacterAttributePower {
    val coefficients = attributePowerCoefficientTable
    val attackPower = parseAttributeInput(attackText) * (1 + (selectedBonus?.attack ?: 0.0)) * coefficients.attack
    val defensePower = parseAttributeInput(defenseText) * (1 + (selectedBonus?.defense ?: 0.0)) * coefficients.defense
    val healthPower = parseAttributeInput(healthText) * (1 + (selectedBonus?.health ?: 0.0)) * coefficients.health
    val speedPower = parseAttributeInput(speedText) * (1 + (selectedBonus?.speed ?: 0.0)) * coefficients.speed

    return CharacterAttributePower(
        attack = attackPower,
        defense = defensePower,
        health = healthPower,
        speed = speedPower
    )
}

fun parseAttributeInput(text: String): Double {
    return text.toDoubleOrNull() ?: 0.0
}

fun calculateGemPowerRows(characterAttributePower: CharacterAttributePower): List<GemPowerRow> {
    val bestAttributePower = listOf(
        characterAttributePower.attack,
        characterAttributePower.defense,
        characterAttributePower.health,
        characterAttributePower.speed
    ).maxOrNull() ?: 0.0
    val levelFiveGemPower = bestAttributePower * 0.18
    val levelTenMaterialGemCount = expectedLevelTenMaterialGemCount()

    return (5..14).map { level ->
        val baseUpgradeGemCount = if (level == 5) {
            1.0
        } else {
            expectedUpgradeGemCountForLevel(level)
        }
        val upgradeGemCount = when {
            level <= 10 -> baseUpgradeGemCount
            else -> baseUpgradeGemCount * levelTenMaterialGemCount
        }
        val powerPerLevelFiveGem = if (level == 5) {
            levelFiveGemPower
        } else {
            val previousLevelPower = levelFiveGemPower * 1.5.pow((level - 6).toDouble())
            val gainedPower = previousLevelPower * 0.5
            if (upgradeGemCount > 0.0) gainedPower / upgradeGemCount else 0.0
        }

        GemPowerRow(
            level = level,
            upgradeGemCount = upgradeGemCount,
            consumedLevelFiveGems = upgradeGemCount,
            powerPerLevelFiveGem = powerPerLevelFiveGem
        )
    }
}

fun expectedUpgradeGemCountForLevel(targetLevel: Int): Double {
    val rule = gemUpgradeRules.firstOrNull { it.targetLevel == targetLevel } ?: return 1.0
    return expectedUpgradeAttempts(rule.successRate, rule.pityFailures)
}

fun expectedLevelTenMaterialGemCount(): Double {
    return (5..10).sumOf { level ->
        if (level == 5) 1.0 else expectedUpgradeGemCountForLevel(level)
    }
}

fun expectedUpgradeAttempts(successRate: Double, pityFailures: Int): Double {
    if (successRate >= 1.0) {
        return 1.0
    }

    val failureRate = 1 - successRate
    val expectedSuccessfulAttempts = (1..pityFailures).sumOf { attempt ->
        attempt * failureRate.pow((attempt - 1).toDouble()) * successRate
    }
    val guaranteedAttempt = pityFailures + 1

    return expectedSuccessfulAttempts +
            guaranteedAttempt * failureRate.pow(pityFailures.toDouble())
}

@Composable
fun AttributeMorningStarScreen(
    onBack: () -> Unit
) {
    SecondaryHomeScreen(
        title = "属性晨星性价比",
        subtitle = "施工中",
        onBack = onBack,
        pinnedTitleBar = true
    ) {
        AttributeMorningStarCalculator()
    }
}

@Composable
fun AttributeMorningStarCalculator() {
    var attackText by remember { mutableStateOf("") }
    var defenseText by remember { mutableStateOf("") }
    var healthText by remember { mutableStateOf("") }
    var speedText by remember { mutableStateOf("") }
    var showExample by remember { mutableStateOf(false) }
    var showGemResult by remember { mutableStateOf(false) }
    var calculatedGemPowerRows by remember { mutableStateOf<List<GemPowerRow>>(emptyList()) }
    var weaponType by remember { mutableStateOf("staff") }
    var selectedJob by remember { mutableStateOf<String?>(null) }
    var selectedTransfer by remember { mutableStateOf<String?>(null) }

    val jobOptions = if (weaponType == "staff") staffJobs else swordJobs
    val transferOptions = remember(selectedJob) {
        if (selectedJob == "法师" || selectedJob == "战士") {
            selectedJob?.let { job ->
                attributeTransferBonusTable
                    .filter { it.jobName == job }
                    .map { transferDisplayText(it) }
            } ?: emptyList()
        } else {
            selectedJob?.let { job ->
                attributeTransferBonusTable
                    .filter { it.jobName == job }
                    .map { transferDisplayText(it) }
            } ?: emptyList()
        }
    }
    val selectedBonus = attributeTransferBonusTable.firstOrNull {
        it.jobName == selectedJob && transferDisplayText(it) == selectedTransfer
    }
    val characterAttributePower = calculateCharacterAttributePower(
        attackText = attackText,
        defenseText = defenseText,
        healthText = healthText,
        speedText = speedText,
        selectedBonus = selectedBonus
    )
    if (showExample) {
        AlertDialog(
            onDismissRequest = { showExample = false },
            confirmButton = {
                TextButton(onClick = { showExample = false }) {
                    Text("关闭")
                }
            },
            text = {
                Image(
                    painter = painterResource(id = R.drawable.character_status_example),
                    contentDescription = "角色属性面板示例图",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                )
            }
        )
    }

    if (showGemResult) {
        GemPowerResultDialog(
            rows = calculatedGemPowerRows,
            onDismiss = { showGemResult = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "角色属性面板",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF222222),
                            modifier = Modifier.weight(1f)
                        )
                        MagnifierButton(onClick = { showExample = true })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CalculatorNumberField("攻击", attackText, { attackText = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    CalculatorNumberField("防御", defenseText, { defenseText = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    CalculatorNumberField("生命", healthText, { healthText = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    CalculatorNumberField("速度", speedText, { speedText = it })
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    WeaponToggleIcon(
                        weaponType = weaponType,
                        onToggle = {
                            weaponType = if (weaponType == "staff") "sword" else "staff"
                            selectedJob = null
                            selectedTransfer = null
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircleDropdown(
                            text = selectedJob ?: "请选择职业",
                            options = jobOptions,
                            onSelect = { job ->
                                selectedJob = job
                                selectedTransfer = if (job == "法师" || job == "战士") {
                                    attributeTransferBonusTable
                                        .firstOrNull { it.jobName == job }
                                        ?.let { transferDisplayText(it) }
                                } else {
                                    null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        CircleDropdown(
                            text = selectedTransfer ?: "请选择转职",
                            options = transferOptions,
                            enabled = selectedJob != null && selectedJob != "法师" && selectedJob != "战士",
                            onSelect = { selectedTransfer = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TransferBonusDiamondPanel(selectedBonus = selectedBonus)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    calculatedGemPowerRows = calculateGemPowerRows(characterAttributePower)
                    showGemResult = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("计算")
            }
        }
    }
}

@Composable
fun GemPowerResultDialog(
    rows: List<GemPowerRow>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = {
            Text(
                text = "宝石战力性价比",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            GemPowerTable(rows = rows)
        }
    )
}

@Composable
fun GemPowerTable(rows: List<GemPowerRow>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 460.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF4F6F9))
            .verticalScroll(rememberScrollState())
    ) {
        GemPowerTableRow(
            levelText = "宝石等级",
            valueText = "战力性价比",
            header = true
        )

        rows.forEachIndexed { index, row ->
            GemPowerTableRow(
                levelText = "${row.level}级宝石",
                valueText = formatNumber(row.powerPerLevelFiveGem),
                alternate = index % 2 == 1
            )
        }
    }
}

@Composable
fun GemPowerTableRow(
    levelText: String,
    valueText: String,
    header: Boolean = false,
    alternate: Boolean = false
) {
    val rowColor = when {
        header -> Color(0xFFE8EDF4)
        alternate -> Color.White
        else -> Color(0xFFF8FAFC)
    }
    val textColor = if (header) Color(0xFF3D4652) else Color(0xFF20242A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor)
            .border(0.5.dp, Color(0xFFDDE3EA))
            .padding(horizontal = 12.dp, vertical = if (header) 10.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = levelText,
            fontSize = if (header) 14.sp else 15.sp,
            fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            fontSize = if (header) 14.sp else 15.sp,
            fontWeight = if (header) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MagnifierButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFFF0F2F5))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(19.dp)) {
            val stroke = 2.2.dp.toPx()
            drawCircle(
                color = Color(0xFF5F6670),
                radius = size.minDimension * 0.32f,
                center = Offset(size.width * 0.42f, size.height * 0.42f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
            drawLine(
                color = Color(0xFF5F6670),
                start = Offset(size.width * 0.64f, size.height * 0.64f),
                end = Offset(size.width * 0.88f, size.height * 0.88f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun WeaponToggleIcon(
    weaponType: String,
    onToggle: () -> Unit
) {
    val iconRes = if (weaponType == "staff") {
        R.drawable.icon_staff
    } else {
        R.drawable.icon_sword
    }

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = if (weaponType == "staff") "杖" else "剑",
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .border(2.dp, Color(0xFFD7D9DE), CircleShape)
            .clickable(onClick = onToggle)
    )
}

@Composable
fun CircleDropdown(
    text: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0xFFD8DEE7) else Color(0xFFC9CDD3))
                .clickable(enabled = enabled && options.isNotEmpty()) {
                    expanded = !expanded
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = circularChoiceText(text),
                color = Color(0xFF222222),
                fontSize = 13.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        FloatingOptionMenu(
            expanded = expanded,
            options = options,
            width = 96.dp,
            onDismiss = { expanded = false },
            onSelect = {
                onSelect(it)
                expanded = false
            }
        )
    }
}

fun circularChoiceText(text: String): String {
    if (text.contains("\n")) {
        return text
    }

    return if (text.length > 3) {
        text.take(3) + "\n" + text.drop(3)
    } else {
        text
    }
}

@Composable
fun FloatingOptionMenu(
    expanded: Boolean,
    options: List<String>,
    width: androidx.compose.ui.unit.Dp,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(width)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.96f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            options.forEach { option ->
                val itemHeight = if (option.contains("\n")) 56.dp else 42.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF333333),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TransferBonusDiamondPanel(selectedBonus: JobTransferBonus?) {
    val attackValue = selectedBonus?.attack ?: 0.0
    val defenseValue = selectedBonus?.defense ?: 0.0
    val healthValue = selectedBonus?.health ?: 0.0
    val speedValue = selectedBonus?.speed ?: 0.0
    val maxValue = listOf(attackValue, defenseValue, healthValue, speedValue)
        .maxOrNull()
        ?.takeIf { it > 0.0 } ?: 0.0
    fun ratio(value: Double): Float {
        return if (maxValue > 0.0) {
            (value / maxValue).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(104.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val top = Offset(size.width / 2f, 0f)
            val right = Offset(size.width, size.height / 2f)
            val bottom = Offset(size.width / 2f, size.height)
            val left = Offset(0f, size.height / 2f)
            val diamond = Path().apply {
                moveTo(top.x, top.y)
                lineTo(right.x, right.y)
                lineTo(bottom.x, bottom.y)
                lineTo(left.x, left.y)
                close()
            }
            drawPath(
                path = diamond,
                color = Color(0xFFE7C2C2).copy(alpha = 0.55f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )

            if (maxValue > 0.0) {
                fun scaledPoint(target: Offset, amount: Float): Offset {
                    return Offset(
                        x = center.x + (target.x - center.x) * amount,
                        y = center.y + (target.y - center.y) * amount
                    )
                }

                val filledShape = Path().apply {
                    val attackPoint = scaledPoint(top, ratio(attackValue))
                    val speedPoint = scaledPoint(right, ratio(speedValue))
                    val healthPoint = scaledPoint(bottom, ratio(healthValue))
                    val defensePoint = scaledPoint(left, ratio(defenseValue))
                    moveTo(attackPoint.x, attackPoint.y)
                    lineTo(speedPoint.x, speedPoint.y)
                    lineTo(healthPoint.x, healthPoint.y)
                    lineTo(defensePoint.x, defensePoint.y)
                    close()
                }

                drawPath(
                    path = filledShape,
                    color = Color(0xFFD69A9A).copy(alpha = 0.42f)
                )
                drawPath(
                    path = filledShape,
                    color = Color(0xFFD29A9A).copy(alpha = 0.9f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
            }

            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = top,
                end = bottom,
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = left,
                end = right,
                strokeWidth = 1.dp.toPx()
            )
        }

        AttributeDiamondLabel(
            title = "攻击",
            value = selectedBonus?.attack,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        AttributeDiamondLabel(
            title = "防御",
            value = selectedBonus?.defense,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        AttributeDiamondLabel(
            title = "速度",
            value = selectedBonus?.speed,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        AttributeDiamondLabel(
            title = "生命",
            value = selectedBonus?.health,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun AttributeDiamondLabel(
    title: String,
    value: Double?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )
        Text(
            text = value?.let { formatPercent(it) } ?: "--",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )
    }
}

data class OriginalStarEffectEntry(
    val stars: Int,
    val effect: String,
    val stepValue: Double,
    val cumulativeValue: Double
)

private const val EFFECT_DUNGEON_REWARD_DOUBLE = "副本评价奖励翻倍概率"

private val originalStarEffectTable = listOf(
    OriginalStarEffectEntry(10, "副本掉落装备升华率", 0.03, 0.03),
    OriginalStarEffectEntry(20, "宝石获取率", 0.05, 0.05),
    OriginalStarEffectEntry(30, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.02),
    OriginalStarEffectEntry(40, "经验获取加成", 0.02, 0.02),
    OriginalStarEffectEntry(56, "副本掉落装备升华率", 0.03, 0.06),
    OriginalStarEffectEntry(72, "宝石获取率", 0.05, 0.10),
    OriginalStarEffectEntry(88, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.04),
    OriginalStarEffectEntry(104, "经验获取加成", 0.02, 0.04),
    OriginalStarEffectEntry(128, "副本掉落装备升华率", 0.03, 0.09),
    OriginalStarEffectEntry(152, "宝石获取率", 0.05, 0.15),
    OriginalStarEffectEntry(176, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.06),
    OriginalStarEffectEntry(200, "经验获取加成", 0.02, 0.06),
    OriginalStarEffectEntry(230, "副本掉落装备升华率", 0.03, 0.12),
    OriginalStarEffectEntry(260, "宝石获取率", 0.05, 0.20),
    OriginalStarEffectEntry(290, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.08),
    OriginalStarEffectEntry(320, "经验获取加成", 0.02, 0.08),
    OriginalStarEffectEntry(360, "副本掉落装备升华率", 0.03, 0.15),
    OriginalStarEffectEntry(400, "宝石获取率", 0.05, 0.25),
    OriginalStarEffectEntry(440, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.10),
    OriginalStarEffectEntry(480, "经验获取加成", 0.02, 0.10),
    OriginalStarEffectEntry(530, "副本掉落装备升华率", 0.03, 0.18),
    OriginalStarEffectEntry(580, "宝石获取率", 0.05, 0.30),
    OriginalStarEffectEntry(630, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.12),
    OriginalStarEffectEntry(680, "经验获取加成", 0.02, 0.12),
    OriginalStarEffectEntry(740, "副本掉落装备升华率", 0.03, 0.21),
    OriginalStarEffectEntry(800, "宝石获取率", 0.05, 0.35),
    OriginalStarEffectEntry(860, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.14),
    OriginalStarEffectEntry(920, "经验获取加成", 0.02, 0.14),
    OriginalStarEffectEntry(990, "副本掉落装备升华率", 0.03, 0.24),
    OriginalStarEffectEntry(1060, "宝石获取率", 0.05, 0.40),
    OriginalStarEffectEntry(1130, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.16),
    OriginalStarEffectEntry(1200, "经验获取加成", 0.02, 0.16),
    OriginalStarEffectEntry(1280, "副本掉落装备升华率", 0.03, 0.27),
    OriginalStarEffectEntry(1360, "宝石获取率", 0.05, 0.45),
    OriginalStarEffectEntry(1440, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.18),
    OriginalStarEffectEntry(1520, "经验获取加成", 0.02, 0.18),
    OriginalStarEffectEntry(1610, "副本掉落装备升华率", 0.03, 0.30),
    OriginalStarEffectEntry(1700, "宝石获取率", 0.05, 0.50),
    OriginalStarEffectEntry(1790, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.20),
    OriginalStarEffectEntry(1880, "经验获取加成", 0.02, 0.20),
    OriginalStarEffectEntry(2000, "副本掉落装备升华率", 0.03, 0.33),
    OriginalStarEffectEntry(2120, "宝石获取率", 0.05, 0.55),
    OriginalStarEffectEntry(2240, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.22),
    OriginalStarEffectEntry(2360, "经验获取加成", 0.02, 0.22),
    OriginalStarEffectEntry(2490, "副本掉落装备升华率", 0.03, 0.36),
    OriginalStarEffectEntry(2620, "宝石获取率", 0.05, 0.60),
    OriginalStarEffectEntry(2750, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.24),
    OriginalStarEffectEntry(2880, "经验获取加成", 0.02, 0.24),
    OriginalStarEffectEntry(3020, "副本掉落装备升华率", 0.03, 0.39),
    OriginalStarEffectEntry(3160, "宝石获取率", 0.05, 0.65),
    OriginalStarEffectEntry(3300, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.26),
    OriginalStarEffectEntry(3440, "经验获取加成", 0.02, 0.26),
    OriginalStarEffectEntry(3590, "副本掉落装备升华率", 0.03, 0.42),
    OriginalStarEffectEntry(3740, "宝石获取率", 0.05, 0.70),
    OriginalStarEffectEntry(3890, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.28),
    OriginalStarEffectEntry(4040, "经验获取加成", 0.02, 0.28),
    OriginalStarEffectEntry(4200, "副本掉落装备升华率", 0.03, 0.45),
    OriginalStarEffectEntry(4360, "宝石获取率", 0.05, 0.75),
    OriginalStarEffectEntry(4520, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.30),
    OriginalStarEffectEntry(4680, "经验获取加成", 0.02, 0.30),
    OriginalStarEffectEntry(4860, "副本掉落装备升华率", 0.03, 0.48),
    OriginalStarEffectEntry(5040, "宝石获取率", 0.05, 0.80),
    OriginalStarEffectEntry(5220, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.32),
    OriginalStarEffectEntry(5400, "经验获取加成", 0.02, 0.32),
    OriginalStarEffectEntry(5600, "副本掉落装备升华率", 0.03, 0.51),
    OriginalStarEffectEntry(5800, "宝石获取率", 0.05, 0.85),
    OriginalStarEffectEntry(6000, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.34),
    OriginalStarEffectEntry(6200, "经验获取加成", 0.02, 0.34),
    OriginalStarEffectEntry(6420, "副本掉落装备升华率", 0.03, 0.54),
    OriginalStarEffectEntry(6640, "宝石获取率", 0.05, 0.90),
    OriginalStarEffectEntry(6860, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.36),
    OriginalStarEffectEntry(7080, "经验获取加成", 0.02, 0.36),
    OriginalStarEffectEntry(7320, "副本掉落装备升华率", 0.03, 0.57),
    OriginalStarEffectEntry(7560, "宝石获取率", 0.05, 0.95),
    OriginalStarEffectEntry(7800, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.38),
    OriginalStarEffectEntry(8040, "经验获取加成", 0.02, 0.38),
    OriginalStarEffectEntry(8300, "副本掉落装备升华率", 0.03, 0.60),
    OriginalStarEffectEntry(8560, "宝石获取率", 0.05, 1.00),
    OriginalStarEffectEntry(8820, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.40),
    OriginalStarEffectEntry(9080, "经验获取加成", 0.02, 0.40),
    OriginalStarEffectEntry(9360, "副本掉落装备升华率", 0.03, 0.63),
    OriginalStarEffectEntry(9640, "宝石获取率", 0.05, 1.05),
    OriginalStarEffectEntry(9920, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.42),
    OriginalStarEffectEntry(10200, "经验获取加成", 0.02, 0.42),
    OriginalStarEffectEntry(10500, "副本掉落装备升华率", 0.03, 0.66),
    OriginalStarEffectEntry(10800, "宝石获取率", 0.05, 1.10),
    OriginalStarEffectEntry(11100, EFFECT_DUNGEON_REWARD_DOUBLE, 0.02, 0.44),
    OriginalStarEffectEntry(11400, "经验获取加成", 0.02, 0.44)
)

data class DungeonProfitResult(
    val fragments: Double,
    val morningStars: Double
)

fun matchDungeonRewardDoubleEntry(input: Int): OriginalStarEffectEntry? {
    return originalStarEffectTable
        .filter { it.effect == EFFECT_DUNGEON_REWARD_DOUBLE && it.stars <= input }
        .maxByOrNull { it.stars }
}

fun calculateGoddessRate(
    star: Int,
    onRack: Boolean
): Double {
    val baseRate = 0.10 + star.coerceIn(0, 5) * 0.04
    return if (onRack) baseRate * 1.2 else baseRate
}

@Composable
fun DungeonMorningStarCalculator() {
    var originalLevelText by remember { mutableStateOf("3300") }
    var goddessStar by remember { mutableIntStateOf(2) }
    var goddessOnRack by remember { mutableStateOf(true) }
    var newDungeonWage by remember { mutableStateOf(false) }
    var abyssWage by remember { mutableStateOf(true) }
    var fragmentPriceText by remember { mutableStateOf("500") }
    var abyssPriceText by remember { mutableStateOf("50") }

    val originalLevel = originalLevelText.toIntOrNull()
    val matchedOriginalEntry = originalLevel?.let { matchDungeonRewardDoubleEntry(it) }
    val originalRate = matchedOriginalEntry?.cumulativeValue
    val goddessRate = calculateGoddessRate(goddessStar, goddessOnRack)
    val fragmentPrice = fragmentPriceText.toDoubleOrNull() ?: 0.0
    val abyssPrice = abyssPriceText.toDoubleOrNull() ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "副本晨星收益计算器",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(12.dp))

            CalculatorNumberField(
                label = "原初档位",
                value = originalLevelText,
                onValueChange = { originalLevelText = it }
            )

            if (originalLevel != null && matchedOriginalEntry != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "副本评价奖励翻倍概率已匹配到 ${matchedOriginalEntry.stars} 档",
                    color = Color(0xFF555555),
                    fontSize = 13.sp
                )
            }

            if (originalLevel != null && matchedOriginalEntry == null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "低于最低原初档位，无法匹配",
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GoddessSettingRow(
                selectedStar = goddessStar,
                onStarChange = { goddessStar = it },
                onRack = goddessOnRack,
                onRackChange = { goddessOnRack = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "基础倍率",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF444444)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ResultLine("原初翻倍率", originalRate?.let { formatPercent(it) } ?: "--")
            ResultLine("女神像翻倍率", formatPercent(goddessRate))

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalculatorNumberField(
                    label = "碎片价格",
                    value = fragmentPriceText,
                    onValueChange = { fragmentPriceText = it },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                CalculatorNumberField(
                    label = "神铸石价格",
                    value = abyssPriceText,
                    onValueChange = { abyssPriceText = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            DungeonProfitSection(
                title = "新本噩梦",
                wageEnabled = newDungeonWage,
                onWageChange = { newDungeonWage = it },
                priceLabel = "碎片价格",
                priceText = fragmentPriceText,
                quantityLabel = "碎片数量期望",
                result = originalRate?.let {
                    calculateDungeonProfit(
                        originalRate = it,
                        goddessRate = goddessRate,
                        wageEnabled = newDungeonWage,
                        price = fragmentPrice,
                        baseFragments = 0.98 * 8 + 0.02 * 50
                    )
                }
            )

            DungeonProfitSection(
                title = "新本炼狱",
                wageEnabled = newDungeonWage,
                onWageChange = { newDungeonWage = it },
                priceLabel = "碎片价格",
                priceText = fragmentPriceText,
                quantityLabel = "碎片数量期望",
                result = originalRate?.let {
                    calculateDungeonProfit(
                        originalRate = it,
                        goddessRate = goddessRate,
                        wageEnabled = newDungeonWage,
                        price = fragmentPrice,
                        baseFragments = 0.972 * 10 + 0.028 * 50
                    )
                }
            )

            DungeonProfitSection(
                title = "君临深渊",
                wageEnabled = abyssWage,
                onWageChange = { abyssWage = it },
                priceLabel = "神铸石价格",
                priceText = abyssPriceText,
                quantityLabel = "神铸石数量期望",
                result = originalRate?.let {
                    calculateDungeonProfit(
                        originalRate = it,
                        goddessRate = goddessRate,
                        wageEnabled = abyssWage,
                        price = abyssPrice,
                        baseFragments = 0.964 * 30 + 0.02 * 150 + 0.016 * 500
                    )
                }
            )
        }
    }
}

fun calculateDungeonProfit(
    originalRate: Double,
    goddessRate: Double,
    wageEnabled: Boolean,
    price: Double,
    baseFragments: Double
): DungeonProfitResult {
    val wageRate = if (wageEnabled) 0.1 else 0.0
    val fragments = (1 + wageRate + goddessRate + originalRate) * baseFragments
    return DungeonProfitResult(
        fragments = fragments,
        morningStars = fragments * price / 10
    )
}

@Composable
fun DungeonProfitSection(
    title: String,
    wageEnabled: Boolean,
    onWageChange: (Boolean) -> Unit,
    priceLabel: String,
    priceText: String,
    quantityLabel: String,
    result: DungeonProfitResult?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { onWageChange(!wageEnabled) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (wageEnabled) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFF9E9E9E)
                        }
                    )
                ) {
                    Text(if (wageEnabled) "工资装：有" else "工资装：无")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ResultLine(priceLabel, priceText.ifBlank { "--" })

            Spacer(modifier = Modifier.height(10.dp))

            ResultLine(quantityLabel, result?.let { formatNumber(it.fragments) } ?: "--")
            ResultLine("晨星期望", result?.let { formatNumber(it.morningStars) } ?: "--")
        }
    }
}

@Composable
fun CalculatorNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String? = null,
    enabled: Boolean = true,
    persistenceKey: String = "calculator_input_$label"
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("check_data", Context.MODE_PRIVATE)
    }
    var isFocused by remember { mutableStateOf(false) }
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }

    LaunchedEffect(persistenceKey) {
        if (enabled && value.isEmpty()) {
            val savedValue = prefs.getString(persistenceKey, "").orEmpty()
            if (savedValue.isNotEmpty()) {
                onValueChange(savedValue)
            }
        }
    }

    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }

    OutlinedTextField(
        value = fieldValue,
        enabled = enabled,
        onValueChange = { newValue ->
            val filteredText = newValue.text.filter { it.isDigit() || it == '.' }
            fieldValue = newValue.copy(
                text = filteredText,
                selection = TextRange(newValue.selection.end.coerceAtMost(filteredText.length))
            )
            onValueChange(filteredText)
            if (enabled) {
                prefs.edit { putString(persistenceKey, filteredText) }
            }
        },
        label = { Text(label) },
        placeholder = if (placeholderText != null && !isFocused) {
            {
                Text(
                    text = placeholderText,
                    color = Color(0xFF9E9E9E)
                )
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    fieldValue = fieldValue.copy(
                        selection = TextRange(0, fieldValue.text.length)
                    )
                }
            }
    )
}

@Composable
fun ValidatedNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true
) {
    Box(modifier = modifier) {
        CalculatorNumberField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            placeholderText = placeholderText,
            enabled = enabled
        )

        if (errorText != null) {
            ValidationBubble(
                text = errorText,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-34).dp)
                    .zIndex(2f)
            )
        }
    }
}

@Composable
fun ValidationBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color(0xEED32F2F), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
            Canvas(modifier = Modifier.size(width = 14.dp, height = 7.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path = path, color = Color(0xEED32F2F))
            }
        }
    }
}

@Composable
fun GoddessSettingRow(
    selectedStar: Int,
    onStarChange: (Int) -> Unit,
    onRack: Boolean,
    onRackChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "幸运神像",
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${selectedStar}星")
                }

                FloatingOptionMenu(
                    expanded = expanded,
                    options = (0..5).map { "${it}星" },
                    width = 112.dp,
                    onDismiss = { expanded = false },
                    onSelect = { option ->
                        onStarChange(option.take(1).toIntOrNull() ?: selectedStar)
                        expanded = false
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { onRackChange(!onRack) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (onRack) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFF9E9E9E)
                    }
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (onRack) "架子上" else "未放架子")
            }
        }
    }
}

@Composable
fun ResultLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF666666),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color(0xFF222222),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatNumber(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

fun formatCoefficient(value: Double): String {
    return String.format(Locale.US, "%.6f", value)
}

fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.1f%%", value * 100)
}

