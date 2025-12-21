package sru.edu.sru_lib_management.infrastructure.route.core_route

import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.DonateHandler

@Configuration
class DonationRouteConfig {

    @Bean
    @FlowPreview
    fun donationRoute(donateHandler: DonateHandler): RouterFunction<ServerResponse> {
        return coRouter {
            (accept(APPLICATION_JSON) and "/api/v1/donation").nest {

                /**
                 * Records new book donation(s) to the library.
                 *
                 * Requires List of [sru.edu.sru_lib_management.core.domain.model.DonationDetail] in request body.
                 * Supports bulk donation recording (multiple books in one request).
                 *
                 * Each [sru.edu.sru_lib_management.core.domain.model.DonationDetail] should contain:
                 * - donationId: Long? - Auto-generated, should be null for new donations
                 * - donorName: String - Name of the donor/sponsor
                 * - donorContact: String? - Contact information (optional)
                 * - bookTitle: String - Title of the donated book
                 * - bookQuantity: Int - Number of copies donated
                 * - donationDate: LocalDate - Date of donation
                 * - notes: String? - Additional notes (optional)
                 *
                 * Process:
                 * 1. Validates donation details
                 * 2. Creates donation records in Database
                 * 3. May trigger book inventory updates
                 * 4. Records donor information for acknowledgment
                 *
                 * Returns list of saved [sru.edu.sru_lib_management.core.domain.model.DonationDetail] entities.
                 * Requires the ADMIN or SUPER_ADMIN role.
                 *
                 * Example request body:
                 * ```json
                 * [
                 *   {
                 *     "donorName": "John Doe Foundation",
                 *     "bookTitle": "Introduction to Physics",
                 *     "bookQuantity": 10,
                 *     "donationDate": "2024-12-21"
                 *   }
                 * ]
                 * ```
                 *
                 * @see DonateHandler.saveDonation
                 * @see sru.edu.sru_lib_management.core.domain.service.DonationService.newDonation
                 * @see sru.edu.sru_lib_management.core.domain.model.DonationDetail
                 */
                POST("", donateHandler::saveDonation)

                /**
                 * Gets all donation records with detailed information.
                 *
                 * Returns [kotlinx.coroutines.flow.Flow] of [sru.edu.sru_lib_management.core.domain.model.DonationDetail] containing:
                 * - Complete donation information
                 * - Donor details (name, contact)
                 * - Book information (title, quantity)
                 * - Donation dates
                 * - Any associated notes
                 *
                 * Useful for:
                 * - Generating donation reports
                 * - Acknowledging donors
                 * - Tracking donation history
                 * - Inventory auditing
                 *
                 * Returns all records sorted by donation date (typically the newest first).
                 *
                 * @see DonateHandler.donationDetailInfo
                 * @see sru.edu.sru_lib_management.core.domain.service.DonationService.getDonationDetail
                 * @see sru.edu.sru_lib_management.core.domain.model.DonationDetail
                 */
                GET("") { donateHandler.donationDetailInfo() }

                /**
                 * Updates an existing donation record.
                 *
                 * Requires [sru.edu.sru_lib_management.core.domain.model.DonationDetail] in request body with donationId.
                 * Used to correct donation information or update notes.
                 *
                 * Updatable fields:
                 * - donorName: Correct donor name spelling
                 * - donorContact: Update contact information
                 * - bookTitle: Fix book title
                 * - bookQuantity: Adjust quantity if miscounted
                 * - donationDate: Correct donation date
                 * - notes: Add or update notes
                 *
                 * Process:
                 * 1. Validates donationId exists
                 * 2. Validates new donation data
                 * 3. Updates donation record
                 * 4. May trigger inventory recalculation if quantity changed
                 *
                 * Returns updated [sru.edu.sru_lib_management.core.domain.model.DonationDetail] entity.
                 * Requires the ADMIN or SUPER_ADMIN role.
                 *
                 * @see DonateHandler.updateDonation
                 * @see sru.edu.sru_lib_management.core.domain.service.DonationService.updateDonation
                 * @see sru.edu.sru_lib_management.core.domain.model.DonationDetail
                 */
                PUT("", donateHandler::updateDonation)
            }
        }
    }
}