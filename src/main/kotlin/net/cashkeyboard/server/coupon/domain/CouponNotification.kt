package net.cashkeyboard.server.coupon.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "coupon_notifications")
class CouponNotification(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    /**
     * Related coupon ID
     */
    @Column(nullable = false)
    val couponId: UUID,

    /**
     * Target user ID
     */
    @Column(nullable = false)
    val userId: UUID,

    /**
     * Device token for FCM
     */
    @Column(nullable = false)
    val deviceToken: String,

    /**
     * Notification title
     */
    @Column(nullable = false)
    var title: String,

    /**
     * Notification body message
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    var body: String,

    /**
     * Additional data payload in JSON format
     */
    @Column(columnDefinition = "TEXT")
    var dataPayload: String? = null,

    /**
     * Notification status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: NotificationStatus = NotificationStatus.PENDING,

    /**
     * When notification was sent
     */
    @Column(nullable = true)
    var sentAt: LocalDateTime? = null,

    /**
     * Error message if sending failed
     */
    @Column(nullable = true)
    var errorMessage: String? = null,

    /**
     * FCM response message ID
     */
    @Column(nullable = true)
    var fcmMessageId: String? = null,

    /**
     * Number of retry attempts
     */
    @Column(nullable = false)
    var retryCount: Int = 0

) : BaseTimeEntity() {

    companion object {
        const val MAX_RETRY_COUNT = 3
    }

    /**
     * Mark notification as sent successfully
     */
    fun markAsSent(fcmMessageId: String) {
        this.status = NotificationStatus.SENT
        this.sentAt = LocalDateTime.now()
        this.fcmMessageId = fcmMessageId
        this.errorMessage = null
    }

    /**
     * Mark notification as failed
     */
    fun markAsFailed(errorMessage: String) {
        this.status = NotificationStatus.FAILED
        this.errorMessage = errorMessage
        this.retryCount++
    }

    /**
     * Mark notification as skipped
     */
    fun markAsSkipped(reason: String) {
        this.status = NotificationStatus.SKIPPED
        this.errorMessage = reason
    }

    /**
     * Check if notification can be retried
     */
    fun canRetry(): Boolean {
        return status == NotificationStatus.FAILED && retryCount < MAX_RETRY_COUNT
    }

    /**
     * Reset for retry
     */
    fun resetForRetry() {
        if (canRetry()) {
            this.status = NotificationStatus.PENDING
            this.errorMessage = null
        }
    }

    /**
     * Check if notification is final (cannot be processed further)
     */
    fun isFinal(): Boolean {
        return status in listOf(NotificationStatus.SENT, NotificationStatus.SKIPPED) ||
                (status == NotificationStatus.FAILED && retryCount >= MAX_RETRY_COUNT)
    }
}
