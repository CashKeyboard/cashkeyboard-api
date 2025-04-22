package net.cashkeyboard.server.user.domain
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByExternalId(externalId: String): Optional<User>
}

@Repository
interface UserDeviceTokenRepository : JpaRepository<UserDeviceToken, UUID> {
    fun findByUserIdAndDeviceToken(userId: UUID, deviceToken: String): Optional<UserDeviceToken>
    fun findAllByUserId(userId: UUID): List<UserDeviceToken>
    fun deleteByUserIdAndDeviceToken(userId: UUID, deviceToken: String)
}