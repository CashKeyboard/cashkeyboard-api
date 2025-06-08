package net.cashkeyboard.server.coupon.domain.exception

import net.cashkeyboard.server.common.errors.ErrorCode
import net.cashkeyboard.server.common.errors.RestApiException
import net.cashkeyboard.server.coupon.domain.CouponStatus
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.*

enum class CouponErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCode {
    // Coupon not found
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "Coupon not found"),

    // Coupon state errors
    COUPON_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "Coupon is not active"),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "Coupon has already been used"),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "Coupon has expired"),
    COUPON_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "Coupon cannot be cancelled"),

    // Purchase errors
    INSUFFICIENT_CASH_FOR_COUPON(HttpStatus.BAD_REQUEST, "Insufficient cash balance for coupon purchase"),
    PRODUCT_NOT_AVAILABLE_FOR_COUPON(HttpStatus.BAD_REQUEST, "Product is not available for coupon purchase"),

    // External service errors
    GIFTICON_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "External gifticon service error"),
    GIFTICON_ISSUE_FAILED(HttpStatus.BAD_REQUEST, "Failed to issue gifticon"),

    // Validation errors
    INVALID_COUPON_DATA(HttpStatus.BAD_REQUEST, "Invalid coupon data"),
    INVALID_EXPIRATION_DATE(HttpStatus.BAD_REQUEST, "Invalid expiration date"),
    INVALID_REFUND_AMOUNT(HttpStatus.BAD_REQUEST, "Invalid refund amount"),

    // Permission errors
    COUPON_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied to this coupon"),
    ADMIN_PERMISSION_REQUIRED(HttpStatus.FORBIDDEN, "Admin permission required for this operation");

    override fun getCode(): String = name
}

// Coupon not found exception
class CouponNotFoundException(couponId: UUID) :
    RestApiException(CouponErrorCode.COUPON_NOT_FOUND)

// Coupon state exceptions
class CouponNotActiveException(couponId: UUID, currentStatus: CouponStatus) :
    RestApiException(CouponErrorCode.COUPON_NOT_ACTIVE)

class CouponAlreadyUsedException(couponId: UUID, usedAt: LocalDateTime) :
    RestApiException(CouponErrorCode.COUPON_ALREADY_USED)

class CouponExpiredException(couponId: UUID, expiresAt: LocalDateTime) :
    RestApiException(CouponErrorCode.COUPON_EXPIRED)

class CouponNotCancellableException(couponId: UUID, currentStatus: CouponStatus) :
    RestApiException(CouponErrorCode.COUPON_NOT_CANCELLABLE)

// Purchase exceptions
class InsufficientCashForCouponException(required: Int, available: Int) :
    RestApiException(CouponErrorCode.INSUFFICIENT_CASH_FOR_COUPON)

class ProductNotAvailableForCouponException(productId: UUID) :
    RestApiException(CouponErrorCode.PRODUCT_NOT_AVAILABLE_FOR_COUPON)

// External service exceptions
class GifticonServiceException(message: String, cause: Throwable? = null) :
    RestApiException(CouponErrorCode.GIFTICON_SERVICE_ERROR)

class GifticonIssueFailedException(reason: String) :
    RestApiException(CouponErrorCode.GIFTICON_ISSUE_FAILED)

// Validation exceptions
class InvalidCouponDataException(message: String) :
    RestApiException(CouponErrorCode.INVALID_COUPON_DATA)

class InvalidExpirationDateException(expirationDate: LocalDateTime) :
    RestApiException(CouponErrorCode.INVALID_EXPIRATION_DATE)

class InvalidRefundAmountException(refundAmount: Int, maxAmount: Int) :
    RestApiException(CouponErrorCode.INVALID_REFUND_AMOUNT)

// Permission exceptions
class CouponAccessDeniedException(couponId: UUID, userId: UUID) :
    RestApiException(CouponErrorCode.COUPON_ACCESS_DENIED)

class AdminPermissionRequiredException(operation: String) :
    RestApiException(CouponErrorCode.ADMIN_PERMISSION_REQUIRED)