package ifmo.se

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

fun main() {
    runBlocking {
        val socket = connect()
        val toServer = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        while (true) {
            print("\t$\t")
            val input = readLine()
            if (input.equals("quit")) {
                socket.close()
                exitProcess(1)
            } else output.writeStringUtf8(input + "\n")

            toServer.read {
                val answer = StandardCharsets.UTF_8.decode(it).split("\n")
                for (line in answer){
                    println("\t>\t $line")
                }
            }
        }
    }
}

private suspend fun connect(
    hostname: String = "localhost",
    port: Int = 5555
): Socket = aSocket(ActorSelectorManager(Dispatchers.Default)).tcp()
    .connect(InetSocketAddress(hostname, port))
