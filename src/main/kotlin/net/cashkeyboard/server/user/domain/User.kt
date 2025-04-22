package net.cashkeyboard.server.user.domain

import jakarta.persistence.*
import net.cashkeyboard.server.common.domain.BaseTimeEntity
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val externalId: String,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var gender: Gender? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var ageRange: AgeRange? = null,
) : BaseTimeEntity() {
    fun updateProfile(name: String, gender: Gender?, ageRange: AgeRange?) {
        this.name = name
        this.gender = gender
        this.ageRange = ageRange
    }
}
