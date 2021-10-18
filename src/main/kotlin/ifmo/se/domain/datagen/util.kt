package ifmo.se.domain.datagen

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ifmo.se.domain.MusicCollection
import ifmo.se.domain.MusicComposition
import java.io.File

fun MusicCollection.toJson(): String {
    val gsonPretty = GsonBuilder().setPrettyPrinting().create()
    return gsonPretty.toJson(this)
}

fun String.fromJson(): MusicCollection {
    return Gson().fromJson(File(this).bufferedReader().use { it.readText() }, MusicCollection::class.java)
}

fun MusicComposition.beautify(): String {
    return "${this.author} - ${this.name}"
}

fun dataLocation() = "${java.nio.file.Paths.get("").toAbsolutePath()}/src/main/resources/data.json"