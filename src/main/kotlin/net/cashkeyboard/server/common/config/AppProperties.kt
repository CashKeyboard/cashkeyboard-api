package net.cashkeyboard.server.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
class AppProperties {
    val auth = Auth()
    val admin = Admin()

    class Auth {
        lateinit var tokenSecret: String
        var tokenExpirationMsec: Long = 86400000  // 24 hours
    }

    class Admin {
        lateinit var secretKey: String
    }
}