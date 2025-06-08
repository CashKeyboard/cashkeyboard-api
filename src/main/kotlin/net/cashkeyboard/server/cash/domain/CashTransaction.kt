package net.cashkeyboard.server.cash.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "cash_transactions")
class CashTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val accountId: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    val balanceAfter: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var source: EarnSource? = null,

    @Column(nullable = true)
    var sourceId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var purpose: SpendPurpose? = null,

    @Column(nullable = true)
    var targetId: String? = null,

    @Column(columnDefinition = "TEXT")
    var metadata: String? = null

) : BaseTimeEntity() {

    fun isEarnTransaction(): Boolean = type in listOf(TransactionType.EARN, TransactionType.RANDOM_EARN)

    fun isSpendTransaction(): Boolean = type == TransactionType.SPEND

    fun isTodayTransaction(): Boolean {
        val today = LocalDate.now()
        return createdAt.toLocalDate() == today
    }
}