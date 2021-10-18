package ifmo.se.domain

data class MusicComposition(val author: String, val name: String)

data class MusicCollection(var musicComps: MutableList<MusicComposition>)