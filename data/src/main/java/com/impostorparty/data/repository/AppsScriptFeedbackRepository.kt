package com.impostorparty.data.repository

import com.impostorparty.data.BuildConfig
import com.impostorparty.data.model.FeedbackApiResponse
import com.impostorparty.data.model.toApiRequest
import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission
import com.impostorparty.domain.repository.FeedbackRepository
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class AppsScriptFeedbackRepository @Inject constructor(
    private val json: Json,
) : FeedbackRepository {

    override suspend fun sendFeedback(submission: FeedbackSubmission): FeedbackSendResult = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.FEEDBACK_ENDPOINT_URL.trim()
        if (endpoint.isBlank()) return@withContext FeedbackSendResult.MissingEndpoint

        val connection = runCatching {
            (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
        }.getOrElse {
            return@withContext FeedbackSendResult.MissingEndpoint
        }

        return@withContext try {
            connection.outputStream.use { output ->
                output.write(json.encodeToString(submission.toApiRequest()).toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val responseBody = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()

            if (statusCode !in 200..299) {
                FeedbackSendResult.ServerError
            } else {
                val response = runCatching {
                    json.decodeFromString(FeedbackApiResponse.serializer(), responseBody)
                }.getOrNull()

                when {
                    response == null -> FeedbackSendResult.InvalidResponse
                    response.ok -> FeedbackSendResult.Success
                    else -> FeedbackSendResult.ServerError
                }
            }
        } catch (_: IOException) {
            FeedbackSendResult.NetworkError
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 15_000
    }
}
