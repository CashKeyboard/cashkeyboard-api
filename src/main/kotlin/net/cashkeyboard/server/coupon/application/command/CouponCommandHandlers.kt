package net.cashkeyboard.server.coupon.application.command

interface CommandHandler<T, R> {
    fun handle(command: T): R
}

interface PurchaseCouponCommandHandler : CommandHandler<PurchaseCouponCommand, PurchaseCouponResult>

interface AdminIssueCouponCommandHandler : CommandHandler<AdminIssueCouponCommand, AdminIssueCouponResult>

interface CancelCouponCommandHandler : CommandHandler<CancelCouponCommand, CancelCouponResult>

interface UseCouponCommandHandler : CommandHandler<UseCouponCommand, UseCouponResult>

interface UpdateGifticonInfoCommandHandler : CommandHandler<UpdateGifticonInfoCommand, UpdateGifticonInfoResult>