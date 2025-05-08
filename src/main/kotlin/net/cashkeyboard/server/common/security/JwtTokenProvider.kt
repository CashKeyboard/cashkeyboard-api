package net.cashkeyboard.server.common.security

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import net.cashkeyboard.server.auth.exception.TokenValidationException
import net.cashkeyboard.server.common.config.AppProperties
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

/**
 * Service for JWT token generation and validation
 */
@Service
class JwtTokenProvider(private val appProperties: AppProperties) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(appProperties.auth.tokenSecret))
    }

    /**
     * Creates a JWT token for the authenticated user
     */
    fun createToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as net.cashkeyboard.server.auth.model.UserPrincipal

        val now = Date()
        val expiryDate = Date(now.time + appProperties.auth.tokenExpirationMsec)

        return Jwts.builder()
            .setSubject(userPrincipal.id.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Creates a JWT token for the specified user ID
     */
    fun createToken(userId: UUID): String {
        val now = Date()
        val expiryDate = Date(now.time + appProperties.auth.tokenExpirationMsec)

        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Extracts user ID from JWT token
     */
    fun getUserIdFromToken(token: String): UUID {
        val claims = parseToken(token)
        return UUID.fromString(claims.subject)
    }

    /**
     * Validates JWT token and returns true if valid
     *
     * @throws TokenValidationException if token is invalid
     */
    fun validateToken(token: String): Boolean {
        try {
            parseToken(token)
            return true
        } catch (ex: SignatureException) {
            throw TokenValidationException("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            throw TokenValidationException("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            throw TokenValidationException("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            throw TokenValidationException("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            throw TokenValidationException("JWT claims string is empty")
        }
    }

    /**
     * Parses JWT token and returns claims
     */
    private fun parseToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}