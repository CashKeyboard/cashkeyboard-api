package net.cashkeyboard.server.coupon.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface CouponRepository : JpaRepository<Coupon, UUID> {

    /**
     * Find coupons by user ID with pagination
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Coupon>

    /**
     * Find coupons by user ID and status
     */
    fun findByUserIdAndStatusOrderByCreatedAtDesc(
        userId: UUID,
        status: CouponStatus,
        pageable: Pageable
    ): Page<Coupon>

    /**
     * Find coupons by user ID and multiple statuses
     */
    @Query("SELECT c FROM Coupon c WHERE c.userId = :userId AND c.status IN :statuses ORDER BY c.createdAt DESC")
    fun findByUserIdAndStatusInOrderByCreatedAtDesc(
        @Param("userId") userId: UUID,
        @Param("statuses") statuses: List<CouponStatus>,
        pageable: Pageable
    ): Page<Coupon>

    /**
     * Find coupons by product ID
     */
    fun findByProductIdOrderByCreatedAtDesc(productId: UUID, pageable: Pageable): Page<Coupon>

    /**
     * Find coupons by issue type
     */
    fun findByIssueTypeOrderByCreatedAtDesc(issueType: CouponIssueType, pageable: Pageable): Page<Coupon>

    /**
     * Find coupons with complex filtering for admin
     */
    @Query("""
        SELECT c FROM Coupon c WHERE 
        (:userId IS NULL OR c.userId = :userId) AND
        (:productId IS NULL OR c.productId = :productId) AND
        (:status IS NULL OR c.status = :status) AND
        (:issueType IS NULL OR c.issueType = :issueType) AND
        (:startDate IS NULL OR c.createdAt >= :startDate) AND
        (:endDate IS NULL OR c.createdAt <= :endDate)
        ORDER BY c.createdAt DESC
    """)
    fun findCouponsWithFilters(
        @Param("userId") userId: UUID?,
        @Param("productId") productId: UUID?,
        @Param("status") status: CouponStatus?,
        @Param("issueType") issueType: CouponIssueType?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
        pageable: Pageable
    ): Page<Coupon>

    /**
     * Find expired but still active coupons
     */
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
    fun findExpiredActiveCoupons(@Param("now") now: LocalDateTime): List<Coupon>

    /**
     * Find coupons by coupon code (for external verification)
     */
    fun findByCouponCode(couponCode: String): Optional<Coupon>

    /**
     * Count coupons by user and status
     */
    fun countByUserIdAndStatus(userId: UUID, status: CouponStatus): Long

    /**
     * Count total coupons by status
     */
    fun countByStatus(status: CouponStatus): Long

    /**
     * Count coupons issued between dates
     */
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    fun countCouponsIssuedBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    /**
     * Sum paid amounts by issue type and date range
     */
    @Query("""
        SELECT COALESCE(SUM(c.paidAmount), 0) FROM Coupon c 
        WHERE c.issueType = :issueType 
        AND c.createdAt BETWEEN :startDate AND :endDate
    """)
    fun sumPaidAmountsByIssueTypeAndDateRange(
        @Param("issueType") issueType: CouponIssueType,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    /**
     * Sum refund amounts by date range
     */
    @Query("""
        SELECT COALESCE(SUM(c.refundAmount), 0) FROM Coupon c 
        WHERE c.status = 'REFUNDED' 
        AND c.cancelledAt BETWEEN :startDate AND :endDate
    """)
    fun sumRefundAmountsByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    /**
     * Find coupons expiring within specified hours
     */
    @Query("""
        SELECT c FROM Coupon c 
        WHERE c.status = 'ACTIVE' 
        AND c.expiresAt BETWEEN :now AND :expirationThreshold
    """)
    fun findCouponsExpiringWithin(
        @Param("now") now: LocalDateTime,
        @Param("expirationThreshold") expirationThreshold: LocalDateTime
    ): List<Coupon>

    /**
     * Update expired coupons status in batch
     */
    @Modifying
    @Query("UPDATE Coupon c SET c.status = 'EXPIRED' WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
    fun markExpiredCoupons(@Param("now") now: LocalDateTime): Int

    /**
     * Find active coupons for user
     */
    @Query("SELECT c FROM Coupon c WHERE c.userId = :userId AND c.status = 'ACTIVE' AND c.expiresAt > :now")
    fun findActiveCouponsForUser(
        @Param("userId") userId: UUID,
        @Param("now") now: LocalDateTime
    ): List<Coupon>

    /**
     * Check if user has active coupon for specific product
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM Coupon c 
        WHERE c.userId = :userId 
        AND c.productId = :productId 
        AND c.status = 'ACTIVE' 
        AND c.expiresAt > :now
    """)
    fun hasActiveCouponForProduct(
        @Param("userId") userId: UUID,
        @Param("productId") productId: UUID,
        @Param("now") now: LocalDateTime
    ): Boolean
}