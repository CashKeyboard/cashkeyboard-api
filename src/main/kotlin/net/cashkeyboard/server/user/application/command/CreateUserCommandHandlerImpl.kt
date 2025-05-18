package net.cashkeyboard.server.user.application.command

import net.cashkeyboard.server.user.domain.User
import net.cashkeyboard.server.user.domain.UserRepository
import net.cashkeyboard.server.user.domain.exception.UserAlreadyExistsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CreateUserCommandHandlerImpl(
    private val userRepository: UserRepository
) : CreateUserCommandHandler {

    @Transactional
    override fun handle(command: CreateUserCommand): UUID {
        if (userRepository.findByExternalId(command.externalId).isPresent) {
            throw UserAlreadyExistsException(command.externalId)
        }

        val user = User(
            externalId = command.externalId,
            name = command.name,
            gender = command.gender,
            ageRange = command.ageRange
        )

        val savedUser = userRepository.save(user)
        return savedUser.id
    }
}