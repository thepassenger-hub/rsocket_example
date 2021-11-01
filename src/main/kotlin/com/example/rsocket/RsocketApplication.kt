package com.example.rsocket

import io.rsocket.Closeable
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.transport.netty.client.TcpClientTransport
import io.rsocket.transport.netty.server.TcpServerTransport
import io.rsocket.transport.netty.server.WebsocketServerTransport
import io.rsocket.util.DefaultPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.rsocket.server.RSocketServer
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.*
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.stereotype.Controller
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.pattern.PathPatternRouteMatcher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.net.URI
import java.time.Duration
import java.time.Instant


@SpringBootApplication
class RsocketApplication

fun main(args: Array<String>) {
    runApplication<RsocketApplication>(*args)
}


data class Message(
    val origin: String? = null,
    val interaction: String? = null,
    val index: Long = 0,
    val created: Long = Instant.now().epochSecond
)

@Configuration
class ClientConfiguration {

    @Bean
    fun diobestia(): RSocketStrategies = RSocketStrategies.builder().encoders{it.addAll(listOf(Jackson2JsonEncoder(), Jackson2CborEncoder()))}.decoders{it.addAll(listOf(Jackson2JsonDecoder(), Jackson2CborDecoder()))}.build()

    @Bean("tcp")
    fun andoniixy() : RSocketRequester {
        return RSocketRequester.builder()
            .rsocketConnector { rSocketConnector: RSocketConnector ->
                rSocketConnector.reconnect(
                    Retry.fixedDelay(
                        2,
                        Duration.ofSeconds(2)
                    )
                )
            }
            .rsocketStrategies(this.diobestia())
            .dataMimeType(MediaType.APPLICATION_CBOR)
            .tcp("localhost", 7000)
    }

    @Bean("ws")
    fun andoniixyz() : RSocketRequester {
        return RSocketRequester.builder()
            .rsocketConnector { rSocketConnector: RSocketConnector ->
                rSocketConnector.reconnect(
                    Retry.fixedDelay(
                        2,
                        Duration.ofSeconds(2)
                    )
                )
            }
            .rsocketStrategies(this.diobestia())
            .dataMimeType(MediaType.APPLICATION_JSON)
            .websocket(URI.create("ws://127.0.0.1:7071"))
//            .tcp("localhost", 7071)
    }

    @Bean("andonio")
    fun aisdjaisdj() =
        RSocketStrategies.builder()
            .encoders{it.addAll(listOf(Jackson2JsonEncoder(), Jackson2CborEncoder()))}
            .decoders{it.addAll(listOf(Jackson2JsonDecoder(), Jackson2CborDecoder()))}
            .routeMatcher(PathPatternRouteMatcher())
            .build()


    @Bean
    fun rsocketMessageHandler() = RSocketMessageHandler().apply {
        rSocketStrategies = aisdjaisdj()
    }


}

@RestController
class RsocketController(
    @Qualifier("tcp")
    val RSocketRequester : RSocketRequester,
    @Qualifier("ws")
    val RSocketRequesterWS : RSocketRequester,
    val handler: RSocketMessageHandler
){

    @GetMapping("/server")
    suspend fun server(): Any {
        io.rsocket.core.RSocketServer.create(handler.responder())
            .bind(WebsocketServerTransport.create("localhost", 7071))
            .awaitSingle()
        return "andonio"
    }

    @GetMapping("/diocane")
    suspend fun testRSocket(): Message {
        val out =  RSocketRequester.route("andonio")
            .data(Message("OIBO", "CLIENT"))
            .retrieveAndAwait<Message>()
        println(out)
        return out
//        return ResponseEntity.ok("OK")
    }

    @GetMapping("/diobestia", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun testRsocket2(): Flow<Message> {
        val ou = RSocketRequester.route("andonio1")
            .data(flowOf(Message("Prova", "Value"),Message("asd", "DELLE CITTA"), Message("xasd", "IMMENSO")))
            .retrieveFlux<Message>()
        return ou.asFlow()
    }

    @GetMapping("/diobestiaws", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun testRsocket3(): Flow<Message> {
        val ou = RSocketRequesterWS.route("andonio1")
            .data(flowOf(Message("Prova", "Value"),Message("asd", "DELLE CITTA"), Message("xasd", "IMMENSO")))
            .retrieveFlux<Message>()
        return ou.asFlow()
    }
//    @MessageMapping("request-response")
//    suspend fun requestResponse(request: Message): Message{
//        println("QQUA")
//        return Message("SERVER", "RESPONSE")
//    }
}

@Controller
class MarketDataRSocketController() {
    @MessageMapping("andonio")
    suspend fun andonio(msg: Mono<Message>): Message {
//        val y = Mono.just()
//        println(y.awaitFirst())
        return Message("SERVER", "RESPONSE")
    }

    @MessageMapping("andonio1")
    suspend fun andonio(msg: Flow<Message>): Flow<Message> {
//        val y = Mono.just()
//        println(y.awaitFirst())
//        msg.asFlow().collect { println(it) }
        return msg.onEach { println(it); delay(1000) }.map { Message(it.interaction, it.origin) }
    }

    @MessageMapping("andonio2")
    suspend fun andonio2(msg: Flow<String>): Flow<Int> {
        println(msg)
//        val y = Mono.just()
//        println(y.awaitFirst())
//        msg.asFlow().collect { println(it) }
        return flow {
            for (i in 0..10)
                emit(i)
        }
    }

    @MessageMapping("andonio3")
    suspend fun andonio3(msg: Flow<String>): Flow<String> {
        println(msg)
//        val y = Mono.just()
//        println(y.awaitFirst())
//        msg.asFlow().collect { println(it) }
        return flow {
//            for (i in 0..10)
            msg.collect {
                emit(it)
            }
        }
    }

    @MessageMapping("andonioBP")
    suspend fun andonioBP(msg: Flow<Int>): Flow<Int> {
//        println(msg)
//        val y = Mono.just()
//        println(y.awaitFirst())
//        msg.asFlow().collect { println(it) }
        return msg.onEach { println("received $it"); delay(2000) }.map { 2*it }
//        return flow {
////            for (i in 0..10)
////            msg.toCharArray().forEach { if (it != '"' ) emit(it.toString()) }
//
//        }.onEach { println(it) }
    }

}
