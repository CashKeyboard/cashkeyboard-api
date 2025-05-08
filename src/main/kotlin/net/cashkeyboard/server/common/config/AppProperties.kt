package net.cashkeyboard.server.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
class AppProperties {
    val auth = Auth()

    class Auth {
        lateinit var tokenSecret: String
        var tokenExpirationMsec: Long = 86400000 // 24시간 기본값
        val authorizedRedirectUris: MutableList<String> = ArrayList()
    }
}