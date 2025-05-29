package net.cashkeyboard.server.common.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.cashkeyboard.server.common.config.AppProperties
import net.cashkeyboard.server.common.errors.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter for admin API authentication using secret key
 */
class AdminAuthenticationFilter(
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(AdminAuthenticationFilter::class.java)
    private val pathMatcher = AntPathMatcher()

    companion object {
        private const val ADMIN_KEY_HEADER = "X-Admin-Key"
        private const val ADMIN_API_PATTERN = "/api/v*/admin/**"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI

        // Check if this is an admin API request
        if (pathMatcher.match(ADMIN_API_PATTERN, requestURI)) {
            logger.debug("Admin API request detected: $requestURI")

            val adminKey = request.getHeader(ADMIN_KEY_HEADER)

            if (adminKey.isNullOrBlank()) {
                logger.warn("Admin API request without admin key: $requestURI")
                sendUnauthorizedResponse(response, "Admin key required")
                return
            }

            if (adminKey != appProperties.admin.secretKey) {
                logger.warn("Invalid admin key provided for request: $requestURI")
                sendUnauthorizedResponse(response, "Invalid admin key")
                return
            }

            logger.debug("Admin authentication successful for request: $requestURI")
        }

        filterChain.doFilter(request, response)
    }

    private fun sendUnauthorizedResponse(response: HttpServletResponse, message: String) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = ErrorResponse.of("ADMIN_UNAUTHORIZED", message)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}