package net.cashkeyboard.server.common.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumValidatorImpl::class])
annotation class ValidEnum(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "Value must be one of: {enumValues}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class EnumValidatorImpl : ConstraintValidator<ValidEnum, String?> {
    private lateinit var enumValues: Array<out Enum<*>>

    override fun initialize(constraintAnnotation: ValidEnum) {
        enumValues = constraintAnnotation.enumClass.java.enumConstants
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true // null은 @NotNull로 별도 처리
        }

        val enumValueNames = enumValues.map { it.name }
        val isValid = enumValueNames.contains(value)

        if (!isValid) {
            // 기본 메시지 비활성화
            context.disableDefaultConstraintViolation()

            // 사용자 정의 메시지
            val enumValuesStr = enumValueNames.joinToString(", ")
            val customMessage = context
                .defaultConstraintMessageTemplate
                .replace("{enumValues}", enumValuesStr)

            context.buildConstraintViolationWithTemplate(customMessage)
                .addConstraintViolation()
        }

        return isValid
    }
}