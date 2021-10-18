package ifmo.se.command

import ifmo.se.domain.MusicCollection
import ifmo.se.domain.MusicComposition
import ifmo.se.domain.datagen.beautify
import ifmo.se.domain.datagen.dataLocation
import ifmo.se.domain.datagen.toJson
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

interface CommandHandler {
    suspend fun handle(mc: MusicCollection)
}

class ListCommandHandler(private val output: ByteWriteChannel) : CommandHandler {
    override suspend fun handle(mc: MusicCollection) {
        val str = StringBuilder()
        str.append("All compositions in catalog: \n")
        mc.musicComps.forEach { str.append(it.beautify()).append("\n") }
        str.toString().send(output)
    }
}

class SearchCommandHandler(private val output: ByteWriteChannel, private val input: ByteReadChannel) : CommandHandler {
    override suspend fun handle(mc: MusicCollection) {
        while (true) {
            val str = StringBuilder()
            "Input the part of the name to find composition in the catalog:\n".send(output)
            val term = input.readUTF8Line()
            if (term != null) {
                mc.musicComps.filter { smartFilter(it, term) }
                    .forEach { str.append(it.beautify()).append("\n") }
                str.toString().send(output)
                break
            }
        }
    }

    private fun smartFilter(item: MusicComposition, term: String): Boolean {
        return item.author.contains(term)
                || item.name.contains(term)
    }
}

class AddCommandHandler(private val output: ByteWriteChannel, private val input: ByteReadChannel) : CommandHandler {
    override suspend fun handle(mc: MusicCollection) {
        while (true) {
            output.writeFully("Input author's name:\n".encodeToByteArray())
            val author = input.readUTF8Line()
            if (author != null) {
                output.writeFully("Input composition's name:\n".encodeToByteArray())
                val name = input.readUTF8Line()
                if (name != null) {
                    val item = MusicComposition(author, name)
                    val current = mc.musicComps
                    current.add(item)

                    mc.musicComps = current
                    mc.save()

                    output.writeFully("'${item.beautify()}' successfully added to the collection.\n".encodeToByteArray())
                    break
                }
            }
        }
    }
}

class DelCommandHandler(private val output: ByteWriteChannel, private val input: ByteReadChannel) : CommandHandler {
    override suspend fun handle(mc: MusicCollection) {
        while (true) {
            "Input the full name of the composition to remove [Author-Composition]:\n".send(output)
            val fullName = input.readUTF8Line()

            if (fullName != null) {
                val parsed = fullName.replace("\\s+", "").split("-")
                val author = parsed[0]
                val name = parsed[1]

                if (mc.musicComps.find { it.author == author && it.name == name } != null) {
                    mc.musicComps.removeIf { it.author == author && it.name == name }
                    mc.save()
                    "'${MusicComposition(author, name).beautify()}' successfully deleted to the collection.\n".send(
                        output
                    )
                } else
                    "${MusicComposition(author, name).beautify()} not found.\n".send(output)

                break
            }
        }
    }
}

class HelpCommandHandler(private val output: ByteWriteChannel) : CommandHandler {
    override suspend fun handle(mc: MusicCollection) {
        helpMessage().send(output)
    }

    private fun helpMessage() =
        """
            Available commands:
                [list] - Show compositions in collection
                [search] - Search composition in collection
                [add] - Add composition to collection
                [del] - Delete composition from collection
                [help] - Show this message 
                [quit] - Quit from current session
        """.trimIndent()
}

private fun MusicCollection.save() = File(dataLocation()).writeText(this.toJson())

private suspend fun String.send(output: ByteWriteChannel) {
    output.writeFully(this.encodeToByteArray())
}
