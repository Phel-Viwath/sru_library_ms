package sru.edu.sru_lib_management.infrastructure.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class WebSocketConfiguration {

    @Bean
    fun webSocketHandlerMapping(
        notificationHandler: NotificationWebSocketHandler
    ): SimpleUrlHandlerMapping {
        val map = mapOf("/notifications" to notificationHandler)
        return SimpleUrlHandlerMapping().apply {
            order = 1 // Sets the order of the handler, lower numbers have higher priority
            urlMap = map
        }
    }

}
