package com.otori.zjcstools

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.util.Locale
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
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        enabled = enabled,
        onValueChange = { text ->
            onValueChange(text.filter { it.isDigit() || it == '.' })
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

fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.1f%%", value * 100)
}

