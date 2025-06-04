package net.cashkeyboard.server.cash.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@Repository
interface CashAccountRepository : JpaRepository<CashAccount, UUID> {
    fun findByUserId(userId: UUID): Optional<CashAccount>

    @Query("SELECT SUM(ca.balance) FROM CashAccount ca")
    fun getTotalCashInSystem(): Long
}

@Repository
interface CashTransactionRepository : JpaRepository<CashTransaction, UUID> {

    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<CashTransaction>

    fun findByUserIdAndTypeOrderByCreatedAtDesc(
        userId: UUID,
        type: TransactionType,
        pageable: Pageable
    ): Page<CashTransaction>

    fun findBySourceIdAndUserId(sourceId: String, userId: UUID): Optional<CashTransaction>

    @Query("""
        SELECT ct FROM CashTransaction ct 
        WHERE ct.userId = :userId 
        AND (:type IS NULL OR ct.type = :type)
        AND (:source IS NULL OR ct.source = :source)
        AND (:startDate IS NULL OR ct.createdAt >= :startDate)
        AND (:endDate IS NULL OR ct.createdAt <= :endDate)
        ORDER BY ct.createdAt DESC
    """)
    fun findTransactionsWithFilters(
        userId: UUID,
        type: TransactionType?,
        source: EarnSource?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable
    ): Page<CashTransaction>

    @Query("""
        SELECT COALESCE(SUM(ct.amount), 0) FROM CashTransaction ct 
        WHERE ct.userId = :userId 
        AND ct.type IN :types
        AND ct.createdAt >= :startDate 
        AND ct.createdAt < :endDate
    """)
    fun sumAmountByUserAndTypesAndDateRange(
        userId: UUID,
        types: List<TransactionType>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Int

    @Query("""
        SELECT COUNT(ct) FROM CashTransaction ct 
        WHERE ct.userId = :userId 
        AND ct.type = :type
        AND ct.createdAt >= :startDate 
        AND ct.createdAt < :endDate
    """)
    fun countByUserAndTypeAndDateRange(
        userId: UUID,
        type: TransactionType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Int
}

@Repository
interface DailyLimitRepository : JpaRepository<DailyLimit, UUID> {

    fun findByUserIdAndDate(userId: UUID, date: LocalDate): Optional<DailyLimit>

    @Query("""
        SELECT dl FROM DailyLimit dl 
        WHERE dl.userId = :userId 
        AND dl.date >= :startDate 
        AND dl.date <= :endDate
        ORDER BY dl.date DESC
    """)
    fun findByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyLimit>

    // 정리용 - 오래된 데이터 삭제
    @Modifying
    @Query("DELETE FROM DailyLimit dl WHERE dl.date < :cutoffDate")
    fun deleteOldRecords(cutoffDate: LocalDate): Int
}