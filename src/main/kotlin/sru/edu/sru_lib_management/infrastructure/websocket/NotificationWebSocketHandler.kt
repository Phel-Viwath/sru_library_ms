package sru.edu.sru_lib_management.infrastructure.websocket

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap

@Component
class NotificationWebSocketHandler : WebSocketHandler {

    private val sink: Sinks.Many<String> = Sinks.many().multicast().onBackpressureBuffer()
    //private val clientSessions = ConcurrentHashMap<String, WebSocketSession>()

    /// old version sent to all user
    override fun handle(session: WebSocketSession): Mono<Void> {
            val input = session.receive()
                .map { it.payloadAsText }
                .doOnNext { println("Received message: $it") }
            val output = session.send(
                sink.asFlux().map(session::textMessage)
            )
            return output.and(input)
    }

    fun sendToAllClient(message: String) {
        sink.tryEmitNext(message)
    }
}