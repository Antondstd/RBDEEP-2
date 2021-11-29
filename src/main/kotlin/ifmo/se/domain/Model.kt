package ifmo.se.domain

import javax.persistence.*

@Entity
@Table(uniqueConstraints = [
    UniqueConstraint(columnNames = ["author", "name"])
])
data class MusicComposition(
    val author: String = "",
    val name: String = ""){
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id:Long = 0

}

data class MusicCollection(var musicComps: MutableList<MusicComposition>)