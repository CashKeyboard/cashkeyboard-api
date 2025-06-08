package net.cashkeyboard.server.cash.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "cash_accounts")
class CashAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val userId: UUID,

    @Column(nullable = false)
    var balance: Int = 0,

    @Column(nullable = false)
    var totalEarned: Int = 0,

    @Column(nullable = false)
    var totalSpent: Int = 0,

    @Column(nullable = true)
    var lastEarnedAt: LocalDateTime? = null,

    @Column(nullable = true)
    var lastSpentAt: LocalDateTime? = null

) : BaseTimeEntity() {

    fun canSpend(amount: Int): Boolean {
        return balance >= amount && amount > 0
    }

    fun earn(amount: Int): CashTransaction {
        require(amount > 0) { "Amount must be positive" }

        balance += amount
        totalEarned += amount
        lastEarnedAt = LocalDateTime.now()

        return CashTransaction(
            accountId = id,
            userId = userId,
            type = TransactionType.EARN,
            amount = amount,
            balanceAfter = balance
        )
    }

    fun earnRandom(amount: Int, source: EarnSource, sourceId: String): CashTransaction {
        require(amount >= 0) { "Amount must be non-negative" }

        if (amount > 0) {
            balance += amount
            totalEarned += amount
            lastEarnedAt = LocalDateTime.now()
        }

        return CashTransaction(
            accountId = id,
            userId = userId,
            type = TransactionType.RANDOM_EARN,
            amount = amount,
            balanceAfter = balance,
            source = source,
            sourceId = sourceId
        )
    }

    fun spend(amount: Int, purpose: SpendPurpose, targetId: String): CashTransaction {
        require(canSpend(amount)) { "Insufficient balance or invalid amount" }

        balance -= amount
        totalSpent += amount
        lastSpentAt = LocalDateTime.now()

        return CashTransaction(
            accountId = id,
            userId = userId,
            type = TransactionType.SPEND,
            amount = amount,
            balanceAfter = balance,
            purpose = purpose,
            targetId = targetId
        )
    }
}