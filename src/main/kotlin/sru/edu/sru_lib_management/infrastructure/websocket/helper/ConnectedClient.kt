package sru.edu.sru_lib_management.infrastructure.websocket.helper

import org.springframework.web.reactive.socket.WebSocketSession
import sru.edu.sru_lib_management.auth.domain.model.Role

data class ConnectedClient(
    val userId: String,
    val role: Role,
    val session: WebSocketSession
)