package net.cashkeyboard.server.user.application.command

import net.cashkeyboard.server.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateUserProfileCommandHandlerImpl(
    private val userRepository: UserRepository
) : UpdateUserProfileCommandHandler {

    @Transactional
    override fun handle(command: UpdateUserProfileCommand) {
        val user = userRepository.findById(command.userId)
            .orElseThrow { IllegalArgumentException("User not found with ID: ${command.userId}") }

        user.updateProfile(
            name = command.name,
            gender = command.gender,
            ageRange = command.ageRange
        )

        userRepository.save(user)
    }
}