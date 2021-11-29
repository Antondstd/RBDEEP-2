package ifmo.se.command


import ifmo.se.MusicService
import ifmo.se.UserService
import ifmo.se.domain.MusicCollection
import ifmo.se.domain.MusicComposition
import ifmo.se.domain.SessionContext
import ifmo.se.domain.UserModel
import ifmo.se.domain.datagen.beautify
import ifmo.se.domain.datagen.dataLocation
import ifmo.se.domain.datagen.toJson
import ifmo.se.send
import io.ktor.utils.io.*
import java.io.File
import kotlin.coroutines.coroutineContext

interface CommandHandler {
    suspend fun handleCommand(mc: MusicCollection)
}

class ListCommandHandler(private val output: ByteWriteChannel) : CommandHandler {
    override suspend fun handleCommand(mc: MusicCollection) {
        val str = StringBuffer()
        val musicCollection = MusicService.getAllMusicCompositions()
        str.appendLine("All compositions in catalog:")
        musicCollection?.forEach { str.appendLine(it.beautify()) }
        send(str.toString().trim(), output)
    }
}

class SearchCommandHandler(private val output: ByteWriteChannel, private val input: ByteReadChannel) : CommandHandler {
    override suspend fun handleCommand(mc: MusicCollection) {
        while (true) {
            "Input the part of the name to find composition in the catalog:".send(output)
            val term = input.readUTF8Line()
            if (term != null) {
                val musicCompositions = MusicService.getMusicCompositionLike(term)
                if (musicCompositions != null && musicCompositions.isNotEmpty()) {
                    val str = StringBuilder()
                    musicCompositions.forEach { str.append(it.beautify()).append("\n") }
                    send(str.toString(), output)
                    return
                }
                send("Didn't found any composition", output)
                return

            }
        }
    }
}

class AddCommandHandler(private val output: ByteWriteChannel, private val input: ByteReadChannel) : CommandHandler {
    override suspend fun handleCommand(mc: MusicCollection) {
        while (true) {
            output.writeFully("Input author's name:".encodeToByteArray())
            val author = input.readUTF8Line()
            if (author != null) {
                output.writeFully("Input composition's name:".encodeToByteArray())
                val name = input.readUTF8Line()
                if (name != null) {
                    val musicComposition = MusicComposition(author, name)
                    MusicService.addMusicComposition(musicComposition)
                    output.writeFully("'${musicComposition.beautify()}' successfully added to the collection.\n".encodeToByteArray())
                    break
                }
            }
        }
    }
}

class DelCommandHandler(private val output: ByteWriteChannel, private val input: ByteReadChannel) : CommandHandler {
    override suspend fun handleCommand(mc: MusicCollection) {
        while (true) {
            "Input the full name of the composition to remove [Author-Composition]:\n".send(output)
            val fullName = input.readUTF8Line()

            if (fullName != null) {
                val parsed = fullName.replace("\\s+", "").split("-")
                val author = parsed[0]
                val name = parsed[1]

                var musicComposition = MusicService.getByAuthorAndName(author, name)
                if (musicComposition != null) {
                    MusicService.deleteById(musicComposition.id)
                    send("${MusicComposition(author, name).beautify()} successfully deleted to the collection",output)
                } else
                    send("${MusicComposition(author, name).beautify()} not found.\n",output)
                return
            }
        }
    }
}

class HelpCommandHandler(private val output: ByteWriteChannel, private val isAuthorizated: Boolean = false) :
    CommandHandler {
    override suspend fun handleCommand(mc: MusicCollection) {
        if (isAuthorizated)
            send(HELP_MESSAGE, output)
        else
            send(HELP_AUTH_MESSAGE, output)
    }

    val HELP_AUTH_MESSAGE = """
            Available commands:
                [register] - add new user
                [login] - log in to the system
                [help] - Show this message 
                [quit] - Quit from current session
        """.trimIndent()

    val HELP_MESSAGE = """
            Available commands:
                [list] - Show compositions in collection
                [search] - Search composition in collection
                [add] - Add composition to collection
                [del] - Delete composition from collection
                [help] - Show this message 
                [quit] - Quit from current session
        """.trimIndent()
}

class AuthorizationCommandHandler(
    private val output: ByteWriteChannel,
    private val input: ByteReadChannel,
    private val type: AuthorizationTypes
) : CommandHandler {

    override suspend fun handleCommand(mc: MusicCollection) {
        val loginAndPassword = askLoginAndPassword()
        if (type == AuthorizationTypes.REGISTER)
            register(loginAndPassword.first, loginAndPassword.second)
        else
            auth(loginAndPassword.first, loginAndPassword.second)
    }

    suspend fun askLoginAndPassword(): Pair<String, String> {
        send("Enter Login:", output)
        val login = input.readUTF8Line()
        send("Enter Password:", output)
        val password = input.readUTF8Line()
        return Pair(login.toString(), password.toString())
    }

    suspend fun register(login: String, password: String) {
        var userModel = UserService.addUser(UserModel(login,password))
        if (userModel != null) {
            send("Successfully registered and logged in as a $login", output)
            coroutineContext[SessionContext]?.login = login
        }
        else
            send("Сould not add a user with this login",output)
    }

    suspend fun auth(login: String, password: String) {
        val user = UserService.getByLoginAndPassword(login,password)
        if (user != null) {
            send("Successfully logged in as a $login", output)
            coroutineContext[SessionContext]?.login = login
        }
        else
            send("Failed to log in, check login and password",output)
    }
}

enum class AuthorizationTypes {
    REGISTER,
    LOGIN
}

class LogInCommandHandler(private val output: ByteWriteChannel, private val input: ByteReadChannel) : CommandHandler {
    override suspend fun handleCommand(mc: MusicCollection) {
        send(coroutineContext[SessionContext]?.login.toString(), output)
    }
}

private fun MusicCollection.save() = File(dataLocation()).writeText(this.toJson())

private suspend fun String.send(output: ByteWriteChannel) {
    output.writeFully(this.encodeToByteArray())
}
