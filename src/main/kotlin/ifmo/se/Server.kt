package ifmo.se

import ifmo.se.command.CommandResolver
import ifmo.se.domain.datagen.dataLocation
import ifmo.se.domain.datagen.fromJson
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

fun main() {
    runBlocking {
        val data = dataLocation().fromJson()
        val server = start()
        println("[x] \t\tServer started [${server.localAddress}]")

        while (true) {
            val client = server.accept()
            println("[x] \t\tClient has been connected [${client.remoteAddress}]")
            launch {
                val input = client.openReadChannel()
                val output = client.openWriteChannel(autoFlush = true)
                try {
                    while (true) {
                        val line = input.readUTF8Line(500)
                        println(">\t $line")
                        CommandResolver(line!!, output, input).resolve().handle(data)
                    }
                } catch (e: Throwable) { }
            }
        }
    }
}

private fun start(
    hostname: String = "localhost",
    port: Int = 5555
): ServerSocket = aSocket(ActorSelectorManager(Dispatchers.Default)).tcp()
    .bind(InetSocketAddress(hostname, port))
