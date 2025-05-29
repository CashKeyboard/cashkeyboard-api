package net.cashkeyboard.server.product.domain

enum class ProductCategory(val displayName: String, val description: String) {
    COFFEE("커피", "커피 및 음료류"),
    FOOD("음식", "음식 및 식품류"),
    GIFT_CARD("상품권", "각종 상품권 및 쿠폰"),
    FASHION("패션", "의류 및 패션 아이템"),
    DIGITAL("디지털", "디지털 콘텐츠 및 서비스"),
    CONVENIENCE("편의점", "편의점 상품"),
    ENTERTAINMENT("엔터테인먼트", "영화, 게임 등 엔터테인먼트"),
    BEAUTY("뷰티", "화장품 및 뷰티 제품"),
    LIFESTYLE("라이프스타일", "생활용품 및 기타"),
    ETC("기타", "분류되지 않은 기타 상품")
}
