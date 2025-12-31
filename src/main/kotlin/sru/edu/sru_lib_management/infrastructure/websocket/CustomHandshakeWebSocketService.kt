package sru.edu.sru_lib_management.infrastructure.websocket

import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class CustomHandshakeWebSocketService(
    upgradeStrategy: RequestUpgradeStrategy
) : HandshakeWebSocketService(upgradeStrategy) {
    override fun handleRequest(exchange: ServerWebExchange, handler: WebSocketHandler): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        // Get the requested subprotocols from the request
        val requestedProtocols = request.headers["Sec-WebSocket-Protocol"]
            ?.flatMap { it.split(",").map { protocol -> protocol.trim() } }
            ?: emptyList()

        println("Requested protocols: $requestedProtocols") // Debug log

        // Check if the handler supports any of the requested protocols
        val supportedProtocols = handler.subProtocols
        println("Supported protocols: $supportedProtocols") // Debug log

        // Find the first matching protocol
        val selectedProtocol = requestedProtocols.firstOrNull { requested ->
            supportedProtocols.any { supported ->
                requested.startsWith(supported, ignoreCase = true)
            }
        }

        // Echo back the selected protocol
        if (selectedProtocol != null) {
            println("Selected protocol: $selectedProtocol") // Debug log
            response.headers.set("Sec-WebSocket-Protocol", selectedProtocol)
        } else {
            println("No matching protocol found!") // Debug log
        }

        return super.handleRequest(exchange, handler)
    }
}