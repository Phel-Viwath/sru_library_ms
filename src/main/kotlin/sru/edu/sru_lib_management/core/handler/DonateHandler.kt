/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.core.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
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

    suspend fun saveDonation(
        request: ServerRequest
    ): ServerResponse = coroutineScope{
        val donationDetails = request.bodyToMono<List<DonationDetail>>().awaitFirstOrNull()
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

    suspend fun donationDetailInfo(request: ServerRequest): ServerResponse {
        val donationDetail =  donationService.getDonationDetail()
        return ServerResponse.ok().bodyAndAwait(donationDetail)
    }

    suspend fun updateDonation(
        request: ServerRequest
    ): ServerResponse = coroutineScope {
        val donationDetail = request.bodyToMono<DonationDetail>().awaitFirstOrNull()
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