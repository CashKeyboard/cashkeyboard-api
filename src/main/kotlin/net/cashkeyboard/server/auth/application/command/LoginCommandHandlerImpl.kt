package net.cashkeyboard.server.auth.application.command

import net.cashkeyboard.server.auth.exception.UnauthorizedException
import net.cashkeyboard.server.common.config.AppProperties
import net.cashkeyboard.server.common.security.JwtTokenProvider
import net.cashkeyboard.server.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginCommandHandlerImpl(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val appProperties: AppProperties
) : LoginCommandHandler {

    @Transactional(readOnly = true)
    override fun handle(command: LoginCommand): LoginResult {
        val user = userRepository.findByExternalId(command.externalId)
            .orElseThrow {
                UnauthorizedException("User not found with external ID: ${command.externalId}")
            }

        val accessToken = jwtTokenProvider.createToken(user.id)

        return LoginResult(
            accessToken = accessToken,
            expiresIn = appProperties.auth.tokenExpirationMsec,
            user = LoginResult.UserInfo(
                id = user.id,
                externalId = user.externalId,
                name = user.name,
                gender = user.gender?.name,
                ageRange = user.ageRange?.name
            )
        )
    }
}