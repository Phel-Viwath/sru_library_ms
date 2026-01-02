package sru.edu.sru_lib_management.infrastructure.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import sru.edu.sru_lib_management.infrastructure.websocket.dashboard.DashboardWebSocketHandler
import sru.edu.sru_lib_management.infrastructure.websocket.notification.NotificationWebSocketHandler

@Configuration
class WebSocketConfiguration {

    @Bean
    fun webSocketHandlerMapping(
        notificationHandler: NotificationWebSocketHandler,
        dashboardHandler: DashboardWebSocketHandler
    ): SimpleUrlHandlerMapping {
        val map = mapOf(
            "/notifications" to notificationHandler,
            "/dashboard" to dashboardHandler
        )
        return SimpleUrlHandlerMapping().apply {
            order = 1 // Sets the order of the handler, lower numbers have higher priority
            urlMap = map
        }
    }


    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }


}
