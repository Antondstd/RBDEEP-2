package ifmo.se

import ifmo.se.command.CommandResolver
import ifmo.se.domain.SessionContext
import ifmo.se.domain.datagen.dataLocation
import ifmo.se.domain.datagen.fromJson
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

fun main() {
    runBlocking {
        val data = dataLocation().fromJson()
        val server = start()
        println("[x] \t\tServer started [${server.localAddress}]")
        val MAX_MESSAGE = 500
        while (true) {
            val client = server.accept()
            println("[x] \t\tClient has been connected [${client.remoteAddress}]")
            launch(SessionContext()) {
                val input = client.openReadChannel()
                val output = client.openWriteChannel(autoFlush = true)
                try {
                    while (true) {
                        val line = input.readUTF8Line(MAX_MESSAGE)
                        println(">\t $line")
                        CommandResolver(line!!, output, input).resolve().handleCommand(data)
                    }
                } catch (e: Throwable) { }
            }
        }
    }
}

suspend fun send(message:String, output: ByteWriteChannel) {
    output.writeFully(message.encodeToByteArray())
}

private fun start(
    hostname: String = "localhost",
    port: Int = 5555
): ServerSocket = aSocket(ActorSelectorManager(Dispatchers.Default)).tcp()
    .bind(InetSocketAddress(hostname, port))
