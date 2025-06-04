package net.cashkeyboard.server.cash.domain.exception

import net.cashkeyboard.server.common.errors.ErrorCode
import net.cashkeyboard.server.common.errors.RestApiException
import org.springframework.http.HttpStatus
import java.util.*

enum class CashErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCode {
    // 계정 관련
    CASH_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Cash account not found"),

    // 잔액 관련
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient cash balance"),

    // 한도 관련
    DAILY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Daily earn limit exceeded"),
    RANDOM_EARN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Daily random earn limit exceeded"),

    // 중복/부정 방지
    DUPLICATE_SOURCE_ID(HttpStatus.CONFLICT, "This source has already been processed"),
    FRAUD_SUSPECTED(HttpStatus.FORBIDDEN, "Fraudulent activity suspected"),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"),

    // 잘못된 요청
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Invalid amount"),
    INVALID_SOURCE(HttpStatus.BAD_REQUEST, "Invalid earn source"),
    INVALID_PURPOSE(HttpStatus.BAD_REQUEST, "Invalid spend purpose");

    override fun getCode(): String = name
}

// 계정 관련 예외
class CashAccountNotFoundException(userId: UUID) :
    RestApiException(CashErrorCode.CASH_ACCOUNT_NOT_FOUND)

// 잔액 관련 예외
class InsufficientBalanceException(
    currentBalance: Int,
    requestedAmount: Int
) : RestApiException(CashErrorCode.INSUFFICIENT_BALANCE)

// 한도 관련 예외
class DailyLimitExceededException(
    currentLimit: Int,
    todayEarned: Int,
    requestedAmount: Int
) : RestApiException(CashErrorCode.DAILY_LIMIT_EXCEEDED)

class RandomEarnLimitExceededException(
    maxDailyCount: Int,
    todayCount: Int
) : RestApiException(CashErrorCode.RANDOM_EARN_LIMIT_EXCEEDED)

// 중복/부정 방지 예외
class DuplicateSourceIdException(
    sourceId: String,
    previousTransactionId: UUID
) : RestApiException(CashErrorCode.DUPLICATE_SOURCE_ID)

class FraudSuspectedException(
    reason: String
) : RestApiException(CashErrorCode.FRAUD_SUSPECTED)

// Rate Limiting 예외
class RateLimitExceededException(
    limitType: String,
    retryAfterSeconds: Long
) : RestApiException(CashErrorCode.RATE_LIMIT_EXCEEDED)

// 잘못된 요청 예외
class InvalidAmountException(amount: Int) :
    RestApiException(CashErrorCode.INVALID_AMOUNT)

class InvalidSourceException(source: String) :
    RestApiException(CashErrorCode.INVALID_SOURCE)

class InvalidPurposeException(purpose: String) :
    RestApiException(CashErrorCode.INVALID_PURPOSE)