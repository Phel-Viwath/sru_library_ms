/*
 * Copyright (c) 2024.
 * @Author Phel Viwath
 */

package sru.edu.sru_lib_management.auth.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sru.edu.sru_lib_management.infrastructure.ApiKeyConfig

@Service
class HunterService @Autowired constructor(
    private val apiKeyConfig: ApiKeyConfig
) {

    private val logger = LoggerFactory.getLogger(HunterService::class.java)
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()

    fun verifyEmail(email: String): String {
        val request = Request.Builder()
            .url("https://api.hunter.io/v2/email-verifier?email=$email&api_key=${apiKeyConfig.apiKey}")
            .build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            responseBody.let {
                val rootNode = objectMapper.readTree(it)
                val resultNode = rootNode.path("data").path("result")
                return resultNode.asText() ?: "Not response"
            }
        }
    }
}