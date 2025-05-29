package net.cashkeyboard.server.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.cashkeyboard.server.auth.exception.TokenValidationException
import net.cashkeyboard.server.user.application.query.GetUserByIdQuery
import net.cashkeyboard.server.user.application.query.GetUserByIdQueryHandler
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that validates JWT tokens and sets authentication in SecurityContext
 */
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val getUserByIdQueryHandler: GetUserByIdQueryHandler
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            if (StringUtils.hasText(jwt)) {
                try {
                    if (jwtTokenProvider.validateToken(jwt)) {
                        val userId = jwtTokenProvider.getUserIdFromToken(jwt)

                        val query = GetUserByIdQuery(userId)
                        val userDto = getUserByIdQueryHandler.handle(query)

                        if (userDto != null) {
                            val authentication = UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                emptyList()
                            )
                            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                            SecurityContextHolder.getContext().authentication = authentication
                        }
                    }
                } catch (ex: TokenValidationException) {
                    logger.error("Could not set user authentication in security context", ex)
                    throw ex
                }
            }

            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
            SecurityContextHolder.clearContext()
            throw ex
        }
    }

    /**
     * Extracts JWT token from request's Authorization header
     */
    private fun getJwtFromRequest(request: HttpServletRequest): String {
        val bearerToken = request.getHeader("Authorization")
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return ""
    }
}