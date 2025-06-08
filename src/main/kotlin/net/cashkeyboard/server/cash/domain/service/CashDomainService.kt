package net.cashkeyboard.server.cash.domain.service

import net.cashkeyboard.server.cash.domain.EarnSource
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class CashDomainService {

    private val random = SecureRandom()

    fun calculateRandomEarn(source: EarnSource): RandomEarnResult {
        val config = getRandomEarnConfig(source)
        val isWinner = random.nextDouble() < config.winRate

        if (!isWinner) {
            return RandomEarnResult(
                isWinner = false,
                amount = 0,
                tier = "NONE",
                winRate = config.winRate,
                possibleAmounts = config.possibleAmounts
            )
        }

        val tier = determineTier()
        val amount = selectAmountByTier(tier, config.possibleAmounts)

        return RandomEarnResult(
            isWinner = true,
            amount = amount,
            tier = tier,
            winRate = config.winRate,
            possibleAmounts = config.possibleAmounts
        )
    }

    private fun getRandomEarnConfig(source: EarnSource): RandomEarnConfig {
        return when (source) {
            EarnSource.LUCKY_SPIN -> RandomEarnConfig(
                winRate = 0.3,
                possibleAmounts = listOf(50, 100, 150, 200, 500, 1000)
            )
            EarnSource.RANDOM_REWARD -> RandomEarnConfig(
                winRate = 0.25,
                possibleAmounts = listOf(30, 80, 120, 300, 800)
            )
            EarnSource.SURPRISE_BONUS -> RandomEarnConfig(
                winRate = 0.15,
                possibleAmounts = listOf(100, 200, 500, 1000, 2000)
            )
            else -> RandomEarnConfig(
                winRate = 0.2,
                possibleAmounts = listOf(50, 100, 200)
            )
        }
    }

    private fun determineTier(): String {
        val tierRandom = random.nextDouble()
        return when {
            tierRandom < 0.05 -> "LEGENDARY"  // 5%
            tierRandom < 0.15 -> "EPIC"       // 10%
            tierRandom < 0.35 -> "RARE"       // 20%
            tierRandom < 0.65 -> "UNCOMMON"   // 30%
            else -> "COMMON"                  // 35%
        }
    }

    private fun selectAmountByTier(tier: String, possibleAmounts: List<Int>): Int {
        val sortedAmounts = possibleAmounts.sorted()
        return when (tier) {
            "LEGENDARY" -> sortedAmounts.lastOrNull() ?: 100
            "EPIC" -> sortedAmounts.getOrNull(sortedAmounts.size - 2) ?: 80
            "RARE" -> sortedAmounts.getOrNull(sortedAmounts.size - 3) ?: 60
            "UNCOMMON" -> sortedAmounts.getOrNull(sortedAmounts.size - 4) ?: 40
            else -> sortedAmounts.firstOrNull() ?: 20
        }
    }

    data class RandomEarnConfig(
        val winRate: Double,
        val possibleAmounts: List<Int>
    )

    data class RandomEarnResult(
        val isWinner: Boolean,
        val amount: Int,
        val tier: String,
        val winRate: Double,
        val possibleAmounts: List<Int>
    )
}
