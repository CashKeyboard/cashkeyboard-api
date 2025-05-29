package net.cashkeyboard.server.common.security

import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object AuthUtil {

    fun getCurrentUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found in security context")

        return authentication.principal as? UUID
            ?: throw IllegalStateException("Invalid principal type in authentication")
    }

    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated && authentication.principal is UUID
    }

    fun isCurrentUser(userId: UUID): Boolean {
        return try {
            getCurrentUserId() == userId
        } catch (e: IllegalStateException) {
            false
        }
    }
}