package net.cashkeyboard.server.user.application.command

import net.cashkeyboard.server.user.domain.UserDeviceToken
import net.cashkeyboard.server.user.domain.UserDeviceTokenRepository
import net.cashkeyboard.server.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateDeviceTokenCommandHandlerImpl(
    private val userRepository: UserRepository,
    private val deviceTokenRepository: UserDeviceTokenRepository
) : UpdateDeviceTokenCommandHandler {

    @Transactional
    override fun handle(command: UpdateDeviceTokenCommand): UUID {
        if (!userRepository.existsById(command.userId)) {
            throw IllegalArgumentException("User not found with ID: ${command.userId}")
        }

        val existingTokenOptional = deviceTokenRepository.findByUserIdAndDeviceToken(
            command.userId,
            command.deviceToken
        )

        if (existingTokenOptional.isPresent) {
            val existingToken = existingTokenOptional.get()

            existingToken.deviceToken = command.deviceToken
            existingToken.deviceType = command.deviceType

            deviceTokenRepository.save(existingToken)
            return existingToken.id
        }
        else {
            val deviceToken = UserDeviceToken(
                userId = command.userId,
                deviceToken = command.deviceToken,
                deviceType = command.deviceType
            )
            val savedToken = deviceTokenRepository.save(deviceToken)

            return savedToken.id
        }

    }
}