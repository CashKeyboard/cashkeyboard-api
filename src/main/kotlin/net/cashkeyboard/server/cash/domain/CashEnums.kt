package net.cashkeyboard.server.cash.domain

enum class TransactionType {
    EARN,         // 일반 적립
    RANDOM_EARN,  // 랜덤 적립
    SPEND         // 사용
}

enum class EarnSource(val displayName: String, val description: String) {
    AD_WATCH("광고 시청", "동영상 광고 시청 완료"),
    MISSION_COMPLETE("미션 완료", "일일/주간 미션 완료"),
    DAILY_BONUS("일일 보너스", "매일 출석 보너스"),
    REFERRAL("추천 보너스", "친구 추천 성공"),
    LUCKY_SPIN("행운의 룰렛", "랜덤 룰렛 게임"),
    RANDOM_REWARD("랜덤 보상", "무작위 보상 이벤트"),
    SURPRISE_BONUS("서프라이즈 보너스", "특별 이벤트 보너스")
}

enum class SpendPurpose(val displayName: String, val description: String) {
    PRODUCT_PURCHASE("상품 구매", "기프티콘 상품 구매"),
    PREMIUM_FEATURE("프리미엄 기능", "프리미엄 기능 이용"),
    GIFT("선물하기", "다른 사용자에게 선물")
}