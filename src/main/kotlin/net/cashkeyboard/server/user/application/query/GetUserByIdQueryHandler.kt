package net.cashkeyboard.server.user.application.query

import net.cashkeyboard.server.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetUserByIdQueryHandlerImpl(
    private val userRepository: UserRepository
) : GetUserByIdQueryHandler {

    @Transactional(readOnly = true)
    override fun handle(query: GetUserByIdQuery): UserDto? {
        return userRepository.findById(query.userId)
            .map { user ->
                UserDto(
                    id = user.id,
                    externalId = user.externalId,
                    name = user.name,
                    gender = user.gender?.name,
                    ageRange = user.ageRange?.name,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )
            }
            .orElse(null)
    }
}