package ifmo.se.domain

import javax.persistence.*

@Entity
data class UserModel(
    @Column(unique = true)
    var login: String = "",
    var password: String = ""
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0
}