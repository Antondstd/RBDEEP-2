package ifmo.se.command

import ifmo.se.domain.SessionContext
import io.ktor.utils.io.*
import kotlin.coroutines.coroutineContext

class CommandResolver(
    var cmd: String,
    private val output: ByteWriteChannel,
    private val input: ByteReadChannel
) {
    suspend fun resolve(): CommandHandler {
        return if (coroutineContext[SessionContext]?.login == null)
            when (cmd) {
                "help" -> HelpCommandHandler(output)
                "register" -> AuthorizationCommandHandler(output, input, AuthorizationTypes.REGISTER)
                "login" -> AuthorizationCommandHandler(output, input, AuthorizationTypes.LOGIN)
                else -> HelpCommandHandler(output)
            }
        else
            when (cmd) {
                "list" -> ListCommandHandler(output)
                "search" -> SearchCommandHandler(output, input)
                "add" -> AddCommandHandler(output, input)
                "del" -> DelCommandHandler(output, input)
                "help" -> HelpCommandHandler(output, true)
                else -> HelpCommandHandler(output)
            }
    }
}