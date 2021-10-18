package ifmo.se.command

import io.ktor.utils.io.*

class CommandResolver(var cmd: String,
                      private val output: ByteWriteChannel,
                      private val input: ByteReadChannel
) {
    fun resolve(): CommandHandler {
        return when (cmd) {
            "list" -> ListCommandHandler(output)
            "search" -> SearchCommandHandler(output, input)
            "add" -> AddCommandHandler(output, input)
            "del" -> DelCommandHandler(output, input)
            "help" -> HelpCommandHandler(output)
            else -> HelpCommandHandler(output)
        }
    }
}