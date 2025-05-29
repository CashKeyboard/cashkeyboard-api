package net.cashkeyboard.server.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import net.cashkeyboard.server.common.errors.ErrorResponse
import net.cashkeyboard.server.common.security.AdminAuthenticationFilter
import net.cashkeyboard.server.common.security.JwtAuthenticationFilter
import net.cashkeyboard.server.common.security.JwtTokenProvider
import net.cashkeyboard.server.user.application.query.GetUserByIdQueryHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.handler.HandlerMappingIntrospector

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val getUserByIdQueryHandler: GetUserByIdQueryHandler,
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
    private val appProperties: AppProperties
) {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtTokenProvider, getUserByIdQueryHandler)
    }

    @Bean
    fun adminAuthenticationFilter(): AdminAuthenticationFilter {
        return AdminAuthenticationFilter(appProperties, objectMapper)
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "X-Auth-Token", "X-Admin-Key")
        configuration.exposedHeaders = listOf("X-Auth-Token")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun customAccessDeniedHandler(): AccessDeniedHandler {
        return AccessDeniedHandler { request, response, accessDeniedException ->
            logger.error("Access denied: ${accessDeniedException.message}")

            response.status = HttpServletResponse.SC_FORBIDDEN
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"

            val errorResponse = ErrorResponse.of("ACCESS_DENIED", accessDeniedException.message ?: "Access denied")
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }

    @Bean
    fun customAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { request, response, authException ->
            logger.error("Authentication failed: ${authException.message}")

            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"

            val errorResponse = ErrorResponse.of("UNAUTHORIZED", authException.message ?: "Authentication required")
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        val mvc = MvcRequestMatcher.Builder(introspector)
        val h2Console = AntPathRequestMatcher("/h2-console/**")

        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .exceptionHandling { exceptions ->
                exceptions
                    .accessDeniedHandler(customAccessDeniedHandler())
                    .authenticationEntryPoint(customAuthenticationEntryPoint())
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    // Public endpoints
                    .requestMatchers(mvc.pattern("/swagger-ui.html")).permitAll()
                    .requestMatchers(mvc.pattern("/swagger-ui/**")).permitAll()
                    .requestMatchers(mvc.pattern("/api-docs/**")).permitAll()
                    .requestMatchers(h2Console).permitAll()
                    .requestMatchers(mvc.pattern("/api/v1/auth/login")).permitAll()
                    .requestMatchers(mvc.pattern("/api/v1/users")).permitAll()

                    // Admin endpoints (will be handled by AdminAuthenticationFilter)
                    .requestMatchers(mvc.pattern("/api/v1/admin/**")).permitAll()

                    // User endpoints (require JWT authentication)
                    .requestMatchers(mvc.pattern("/api/v1/products")).authenticated()
                    .requestMatchers(mvc.pattern("/api/v1/**")).authenticated()

                    .anyRequest().authenticated()
            }
            .addFilterBefore(adminAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        http.headers { headers -> headers.frameOptions { it.disable() } }

        return http.build()
    }
}