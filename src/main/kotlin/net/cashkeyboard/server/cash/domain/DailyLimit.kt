package net.cashkeyboard.server.cash.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "daily_limits")
class DailyLimit(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val date: LocalDate,

    @Column(nullable = false)
    var todayEarned: Int = 0,

    @Column(nullable = false)
    var todayEarnedCount: Int = 0,

    @Column(nullable = false)
    var todayRandomEarnedCount: Int = 0,

    @Column(nullable = false)
    var todaySpent: Int = 0,

    // Rate limiting을 위한 필드들
    @Column(nullable = true)
    var lastEarnAt: LocalDateTime? = null,

    @Column(nullable = true)
    var lastRandomEarnAt: LocalDateTime? = null,

    @Column(nullable = true)
    var lastSpendAt: LocalDateTime? = null

) : BaseTimeEntity() {

    companion object {
        const val MAX_DAILY_EARN = 1000
        const val MAX_DAILY_EARN_COUNT = 20
        const val MAX_RANDOM_EARN_COUNT = 10
        const val EARN_RATE_LIMIT_SECONDS = 60L
        const val RANDOM_EARN_RATE_LIMIT_SECONDS = 60L
        const val SPEND_RATE_LIMIT_SECONDS = 30L
    }

    fun canEarn(amount: Int): Boolean {
        return todayEarned + amount <= MAX_DAILY_EARN &&
                todayEarnedCount < MAX_DAILY_EARN_COUNT
    }

    fun canRandomEarn(): Boolean {
        return todayRandomEarnedCount < MAX_RANDOM_EARN_COUNT
    }

    fun isEarnRateLimited(): Boolean {
        return lastEarnAt?.let {
            Duration.between(it, LocalDateTime.now()).seconds < EARN_RATE_LIMIT_SECONDS
        } ?: false
    }

    fun isRandomEarnRateLimited(): Boolean {
        return lastRandomEarnAt?.let {
            Duration.between(it, LocalDateTime.now()).seconds < RANDOM_EARN_RATE_LIMIT_SECONDS
        } ?: false
    }

    fun isSpendRateLimited(): Boolean {
        return lastSpendAt?.let {
            Duration.between(it, LocalDateTime.now()).seconds < SPEND_RATE_LIMIT_SECONDS
        } ?: false
    }

    fun recordEarn(amount: Int) {
        require(canEarn(amount)) { "Daily earn limit exceeded" }

        todayEarned += amount
        todayEarnedCount++
        lastEarnAt = LocalDateTime.now()
    }

    fun recordRandomEarn() {
        require(canRandomEarn()) { "Daily random earn limit exceeded" }

        todayRandomEarnedCount++
        lastRandomEarnAt = LocalDateTime.now()
    }

    fun recordSpend(amount: Int) {
        todaySpent += amount
        lastSpendAt = LocalDateTime.now()
    }

    fun getRemainingEarnLimit(): Int {
        return maxOf(0, MAX_DAILY_EARN - todayEarned)
    }

    fun getRemainingEarnCount(): Int {
        return maxOf(0, MAX_DAILY_EARN_COUNT - todayEarnedCount)
    }

    fun getRemainingRandomEarnCount(): Int {
        return maxOf(0, MAX_RANDOM_EARN_COUNT - todayRandomEarnedCount)
    }
}