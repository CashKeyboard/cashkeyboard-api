package net.cashkeyboard.server.user.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import java.util.*

@Entity
@Table(name = "user_device_tokens")
class UserDeviceToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    var deviceToken: String,

    @Column(nullable = false)
    var deviceType: String,
) : BaseTimeEntity()