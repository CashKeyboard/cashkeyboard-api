package net.cashkeyboard.server.coupon.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.*


@Repository
interface CouponNotificationRepository : JpaRepository<CouponNotification, UUID> {

    /**
     * Find pending notifications for processing
     */
    fun findByStatusOrderByCreatedAtAsc(status: NotificationStatus): List<CouponNotification>

    /**
     * Find notifications by coupon ID
     */
    fun findByCouponIdOrderByCreatedAtDesc(couponId: UUID): List<CouponNotification>

    /**
     * Find notifications by user ID
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<CouponNotification>

    /**
     * Find failed notifications that can be retried
     */
    @Query("SELECT cn FROM CouponNotification cn WHERE cn.status = 'FAILED' AND cn.retryCount < :maxRetryCount")
    fun findRetryableFailedNotifications(@Param("maxRetryCount") maxRetryCount: Int = CouponNotification.MAX_RETRY_COUNT): List<CouponNotification>

    /**
     * Count notifications by status and date range
     */
    @Query("""
        SELECT COUNT(cn) FROM CouponNotification cn 
        WHERE cn.status = :status 
        AND cn.createdAt BETWEEN :startDate AND :endDate
    """)
    fun countByStatusAndDateRange(
        @Param("status") status: NotificationStatus,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    /**
     * Delete old processed notifications
     */
    @Modifying
    @Query("""
        DELETE FROM CouponNotification cn 
        WHERE cn.status IN ('SENT', 'SKIPPED') 
        AND cn.createdAt < :cutoffDate
    """)
    fun deleteOldProcessedNotifications(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
}