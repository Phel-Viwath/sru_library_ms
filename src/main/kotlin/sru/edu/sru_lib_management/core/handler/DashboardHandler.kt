/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import sru.edu.sru_lib_management.core.domain.service.DashboardService

@Component
class DashboardHandler(
    private val dashboardService: DashboardService,
){
    //private var card: List<CardData>? = null

    //private val logger = LoggerFactory.getLogger(DashboardHandler::class.java)

    //http://localhost:8090/api/v1/dashboard
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun dashboard(): ServerResponse {
        return coroutineScope {
            val response = dashboardService.getDashboardData()
            ServerResponse.ok().bodyValue(response).awaitSingle()
        }

    }

}