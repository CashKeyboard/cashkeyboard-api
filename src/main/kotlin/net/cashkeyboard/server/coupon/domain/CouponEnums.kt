package net.cashkeyboard.server.coupon.domain

/**
 * Coupon status enumeration
 */
enum class CouponStatus(val displayName: String, val description: String) {
    ACTIVE("활성", "사용 가능한 상태"),
    USED("사용됨", "이미 사용된 상태"),
    EXPIRED("만료됨", "유효기간 만료"),
    CANCELLED("취소됨", "관리자에 의해 취소된 상태"),
    REFUNDED("환불됨", "환불 처리된 상태")
}

/**
 * Coupon issue type enumeration
 */
enum class CouponIssueType(val displayName: String, val description: String) {
    PURCHASE("구매", "사용자가 캐시로 구매한 쿠폰"),
    ADMIN_ISSUE("관리자 발급", "관리자가 직접 발급한 쿠폰"),
    PROMOTION("프로모션", "이벤트 또는 프로모션으로 발급된 쿠폰"),
    COMPENSATION("보상", "문제 해결 보상으로 발급된 쿠폰")
}

/**
 * Notification status for FCM push notifications
 */
enum class NotificationStatus(val displayName: String, val description: String) {
    PENDING("대기", "알림 발송 대기 중"),
    SENT("발송됨", "알림 발송 완료"),
    FAILED("실패", "알림 발송 실패"),
    SKIPPED("건너뜀", "알림 발송 건너뜀")
}