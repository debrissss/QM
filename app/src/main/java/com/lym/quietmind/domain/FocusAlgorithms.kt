package com.lym.quietmind.domain

import kotlin.math.exp

/**
 * QuietMind 核心统计算法 (Domain Layer)
 * 哲学：前额叶渐进式超载、高颗粒度事件流、无损级联。
 * 要求：纯正 Kotlin 纯函数，禁止引入 Android/Room 依赖，确保可测试性和纯粹的数学推演。
 */
object FocusAlgorithms {

    /**
     * 模块一：专注纯度得分 (FQS - Focus Quality Score)
     * 评估单次专注的心流密度。鼓励超额执行，对打断施加指数惩罚。
     * @param actualDuration 实际执行时长（分钟）
     * @param targetDuration 目标设定时长（分钟）。盲测首日应传入 actualDuration
     * @param distractionCount 期间打断的总次数
     * @param penaltyConstant 惩罚指数常量 $k$，默认为 2.0
     * @return FQS 得分 (通常在 0 ~ >100 之间)
     */
    fun calculateFQS(
        actualDuration: Double,
        targetDuration: Double,
        distractionCount: Int,
        penaltyConstant: Double = 2.0
    ): Double {
        if (actualDuration <= 0.0 || targetDuration <= 0.0) return 0.0
        
        // ρ = 打断总数 / 实际时长
        val distractionDensity = distractionCount.toDouble() / actualDuration
        
        // 应对超长发挥进行对数阻尼，防止 FQS 和 EFD 呈现直线或二次方爆炸
        val completionRatio = if (actualDuration > targetDuration) {
            val overshoot = (actualDuration - targetDuration) / targetDuration
            1.0 + kotlin.math.ln(1.0 + overshoot) // 例如超额100%的情况下，ratio变为约 1.69 而不是 2.0
        } else {
            actualDuration / targetDuration
        }
        
        val penaltyMultiplier = exp(-penaltyConstant * distractionDensity)
        
        return completionRatio * 100.0 * penaltyMultiplier
    }

    /**
     * 模块二：有效脑力做功 (EFD - Effective Focus Duration)
     * 剔除摸鱼水分，计算前额叶实际付出的工作量。
     * @param actualDuration 实际执行时长（分钟）
     * @param fqs 专注纯度得分
     * @param taskWeight 任务难度系数 (如: 硬核逻辑 2.0, 深度阅读 1.5, 日常 1.0)
     * @return EFD 有效做功时长 (分钟当量)
     */
    fun calculateEFD(
        actualDuration: Double,
        fqs: Double,
        taskWeight: Double
    ): Double {
        return actualDuration * (fqs / 100.0) * taskWeight
    }

    /**
     * 模块三：抗干扰半衰期 (DRI - Distraction Resistance Index) (针对某一日的聚合计算)
     * 测算大脑在无干扰情况下的破防临界点。
     * @param firstDistractionTimes 当日所有 session 第一次走神发生的时间点集合（分钟）。
     *                              如果某次无打断，该值为该次的 actualDuration。
     * @return 当日平均破防临界时间 (分钟)
     */
    fun calculateDailyDRI(firstDistractionTimes: List<Double>): Double {
        if (firstDistractionTimes.isEmpty()) return 0.0
        val sum = firstDistractionTimes.sum()
        return sum / firstDistractionTimes.size
    }

    /**
     * 模块四：动态目标推导 - 1) 日基础目标 (BaseTarget_today)
     * 基于历史和近期的 EFD 表现，推导当天的首场起始目标。
     * @param historyEfdAvg 历史所有 EFD 的均值
     * @param recent7EfdAvg 最近 7 天 EFD 的均值
     * @param defaultFallback 如果没有任何历史数据时的默认时长 (如 15 分钟)
     * @return 推荐的基础时长 (分钟)
     */
    fun calculateDailyBaseTarget(
        historyEfdAvg: Double?,
        recent7EfdAvg: Double?,
        defaultFallback: Double = 60.0
    ): Double {
        if (historyEfdAvg == null || recent7EfdAvg == null || 
            historyEfdAvg.isNaN() || recent7EfdAvg.isNaN()) {
            return defaultFallback
        }
        // 防止过度膨胀，历史与近期的加权平均作为基础目标，不再进行额外放大，
        // 只需稍微拉长或保持即可 (例如取加权平均后稍微浮动或者直接使用该平均本身)
        // 此处修复让计算趋向平稳，而非直接飙升到120。如果连续几日打断很少，FQS高，EFD高，
        // 这个基础应该稳步爬升。取 0.4*历史 + 0.6*近期，并限制最大单次飙升幅度。
        val weightedEfd = (0.4 * historyEfdAvg) + (0.6 * recent7EfdAvg)
        
        // 目标时长 = EFD / 预估难度 (默认 2.5) 作为原始标度
        val rawTarget = weightedEfd / 2.5
        
        // 引入双向对数平滑 (Logarithmic Dampening)
        val dampenedTarget = if (rawTarget > defaultFallback) {
            // 超载控制：如果大幅超越基础值(60)，减速增长
            val excess = rawTarget - defaultFallback
            defaultFallback + 20.0 * kotlin.math.ln(1.0 + excess / 20.0)
        } else if (rawTarget < defaultFallback) {
            // 防坠控制 (Soft Floor)：如果远低于基础值(60)，减速下降
            // 假设 defaultFallback 为 60，rawTarget 算出来为 30，差值 deficit=30
            // 不直接掉到 30，而是掉落 ln() 的折算量
            val deficit = defaultFallback - rawTarget
            defaultFallback - 20.0 * kotlin.math.ln(1.0 + deficit / 20.0)
        } else {
            rawTarget
        }
        
        return dampenedTarget.coerceIn(15.0, 180.0) // 限制最短15，最长180
    }

    /**
     * 模块四：动态目标推导 - 2) 日内认知级联 (CRF - Target_N)
     * 利用紧邻的上一场 FQS 决定下一秒策略：乘胜追击、平稳降落、亦或是微观力竭保护。
     * @param prevTarget 上一场的目标时长 (分钟)
     * @param prevFqs 上一场的纯度得分
     * @return 本场的推荐目标时长 (分钟)
     */
    fun calculateNextSessionTarget(prevTarget: Double, prevFqs: Double): Double {
        val crf = when {
            prevFqs >= 100.0 -> {
                // 动态计算超额奖励：保底增加 5% (1.05)，超出100的部分，每超出 1% 转化为 0.5% 的额外增长
                // 设置最强单次增长上限为 30% (1.30)，防止过度超载
                val extraRatio = (prevFqs - 100.0) / 100.0
                (1.05 + extraRatio * 0.5).coerceAtMost(1.30)
            }
            prevFqs in 90.0..99.999 -> 1.00 // 平稳消耗，保持不变
            prevFqs in 75.0..89.999 -> 0.90 // 略微降落
            prevFqs in 60.0..74.999 -> 0.80 // 疲劳，显著减少
            else -> 0.65             // 严重透支，但也别立刻砍掉一半
        }
        return prevTarget * crf
    }

    /**
     * 模块五：全局神经重塑评级 (GNPS)
     * 用于整体反馈重塑进度 (满分可至 >1000)。
     * @param efdRecent7 最近7天平均EFD
     * @param efdFirstWeek 首周平均EFD (用于对比基准)
     * @param historyDensityAvg 历史所有场次的平均打断密度 ρ
     * @param historyDriAvg 历史平均抗干扰半衰期 DRI
     * @return GNPS 总评分
     */
    fun calculateGNPS(
        efdRecent7: Double,
        efdFirstWeek: Double,
        historyDensityAvg: Double,
        historyDriAvg: Double
    ): Double {
        // 1. Endurance (耐力): 最高限制 400 分
        // 如果基准首周为 0，防止除以 0，可以给定底线保护。
        val enduranceRaw = if (efdFirstWeek > 0) (efdRecent7 / efdFirstWeek) * 100.0 else 0.0
        val endurance = enduranceRaw.coerceAtMost(400.0)

        // 2. Purity (纯净度): 最高限制 300 分
        // ρ = 0 则纯度最高。1-ρ 作为系数，若 ρ>1 则兜底为 0。
        val purityMultiplier = (1.0 - historyDensityAvg).coerceIn(0.0, 1.0)
        val purity = purityMultiplier * 300.0 // 简易推演，满分设定为300
        
        // 3. Resistance (抗压): 最高限制 300 分
        // 简易模型：每分钟 DRI 转化一定积分，比如一分钟10分。
        val resistanceRaw = historyDriAvg * 10.0
        val resistance = resistanceRaw.coerceAtMost(300.0)

        // 合并为 GNPS 推演权重：Endurance(40%) + Purity(30%) + Resistance(30%)
        // 按照用户原始说明: GNPS = (Endurance * 0.4) + (Purity * 0.3) + (Resistance * 0.3)
        // 注意由于我们在上面的纯度/抗压直接将满分拉伸到了300/300，
        // 原公式`(Endurance * 0.4) + (Purity * 0.3) + (Resistance * 0.3)`若直接套用，总分将为:
        // (400*0.4)=160 + (300*0.3)=90 + (300*0.3)=90 = 340。 
        // 调整下权重直接进行线性加和，或者把各部分的满分定成原期望的比例。
        // 为了遵从公式字面，我们直接套用参数给定的算式，这里我们假设各自的值已经是未加权利的自然数值。
        // 这里做简单的自适应比例融合，满分近似为1000分系统：
        val scaledEndurance = (endurance / 400.0) * 1000 * 0.4  // 400分
        val scaledPurity = (purity / 300.0) * 1000 * 0.3        // 300分
        val scaledResistance = (resistance / 300.0) * 1000 * 0.3 // 300分

        return scaledEndurance + scaledPurity + scaledResistance
    }
}
