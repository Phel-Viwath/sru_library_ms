/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import sru.edu.sru_lib_management.common.CoreResult
import sru.edu.sru_lib_management.core.domain.model.DonationDetail
import sru.edu.sru_lib_management.core.domain.service.DonationService
import sru.edu.sru_lib_management.utils.ResponseStatus.BAD_REQUEST
import sru.edu.sru_lib_management.utils.ResponseStatus.CREATED
import sru.edu.sru_lib_management.utils.ResponseStatus.INTERNAL_SERVER_ERROR

@Component
class DonateHandler(
    private val donationService: DonationService
) {

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun saveDonation(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val donationDetails: List<DonationDetail> = request.bodyToMono<List<DonationDetail>>().awaitFirstOrNull()
            ?: return@coroutineScope ServerResponse.badRequest().bodyValueAndAwait("Invalid donation details")
        when(val result = donationService.newDonation(donationDetails)){
            is CoreResult.Success ->
                ServerResponse.status(CREATED).bodyValueAndAwait(result.data)
            is CoreResult.Failure ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.errorMsg)
            is CoreResult.ClientError ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.clientErrMsg)
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    suspend fun donationDetailInfo(): ServerResponse {
        val donationDetail =  donationService.getDonationDetail()
        return ServerResponse.ok().bodyAndAwait(donationDetail)
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    suspend fun updateDonation(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val donationDetail: DonationDetail = request.bodyToMono<DonationDetail>().awaitFirstOrNull()
            ?: return@coroutineScope ServerResponse.badRequest().bodyValueAndAwait("Invalid donation detail")

        when(val result = donationService.updateDonation(donationDetail)){
            is CoreResult.Success ->
                ServerResponse.status(CREATED).bodyValueAndAwait(result.data)
            is CoreResult.ClientError ->
                ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValueAndAwait(result.clientErrMsg)
            is CoreResult.Failure ->
                ServerResponse.status(BAD_REQUEST).bodyValueAndAwait(result.errorMsg)
        }
    }

}