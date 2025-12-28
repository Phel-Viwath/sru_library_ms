package sru.edu.sru_lib_management.infrastructure.websocket

import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.core.handler.DashboardWebSocketHandler

/**
* Service for triggering real-time dashboard updates via WebSocket.
* Inject this service into any handler or service that modifies data
* affecting the dashboard.
*/

@Service class DashboardNotificationService(
    private val dashboardWebSocketHandler: DashboardWebSocketHandler
) {
    /**  Triggers a complete dashboard refresh.
     * Use this when multiple metrics may have changed.
     *
     * Example use cases:
     * - New attendance recorded
     * - Book borrowed or returned
     * - Donation received
     **/
    fun notifyDashboardUpdate() {
        dashboardWebSocketHandler.refreshDashboard()
    }
}