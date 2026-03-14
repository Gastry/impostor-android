package com.impostorparty.data.repository

import com.impostorparty.data.BuildConfig
import com.impostorparty.data.model.FeedbackApiRequest
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
class AppsScriptFeedbackRepository internal constructor(
    private val json: Json,
    private val endpoint: String,
    private val sharedToken: String,
    private val openConnection: (String) -> HttpURLConnection,
) : FeedbackRepository {

    @Inject
    constructor(json: Json) : this(
        json = json,
        endpoint = BuildConfig.FEEDBACK_ENDPOINT_URL,
        sharedToken = BuildConfig.FEEDBACK_SHARED_TOKEN,
        openConnection = { url -> URL(url).openConnection() as HttpURLConnection },
    )

    override suspend fun sendFeedback(submission: FeedbackSubmission): FeedbackSendResult = withContext(Dispatchers.IO) {
        val resolvedEndpoint = endpoint.trim()
        val resolvedToken = sharedToken.trim()
        if (!isConfigurationValid(resolvedEndpoint, resolvedToken)) {
            return@withContext FeedbackSendResult.MissingConfiguration
        }

        val request = submission.toApiRequest(sharedToken = resolvedToken)
        if (!isValidRequest(request)) {
            return@withContext FeedbackSendResult.InvalidRequest
        }

        val connection = runCatching {
            openConnection(resolvedEndpoint).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("X-Feedback-Client", "el-impostor-android")
            }
        }.getOrElse {
            return@withContext FeedbackSendResult.MissingConfiguration
        }

        return@withContext try {
            connection.outputStream.use { output ->
                output.write(json.encodeToString(request).toByteArray(Charsets.UTF_8))
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
        const val MIN_MESSAGE_LENGTH = 8
        const val MAX_MESSAGE_LENGTH = 1_200
        const val MIN_SHARED_TOKEN_LENGTH = 16
        const val MAX_SHARED_TOKEN_LENGTH = 128
        const val MAX_APP_VERSION_LENGTH = 64
        const val MAX_LOCALE_LENGTH = 35
        const val MIN_PLAYER_COUNT = 3
        const val MAX_PLAYER_COUNT = 20
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val LOCALE_REGEX = Regex("^[A-Za-z]{2,3}([_-][A-Za-z0-9]{2,8})*$")
        val ALLOWED_TYPES = setOf("suggestion", "problem")
    }

    private fun isConfigurationValid(endpoint: String, token: String): Boolean {
        return endpoint.startsWith("https://") &&
            token.length in MIN_SHARED_TOKEN_LENGTH..MAX_SHARED_TOKEN_LENGTH
    }

    private fun isValidRequest(request: FeedbackApiRequest): Boolean {
        val message = request.message.trim()
        val email = request.email?.trim().orEmpty()

        return request.type in ALLOWED_TYPES &&
            request.token.length in MIN_SHARED_TOKEN_LENGTH..MAX_SHARED_TOKEN_LENGTH &&
            message.length in MIN_MESSAGE_LENGTH..MAX_MESSAGE_LENGTH &&
            (email.isEmpty() || EMAIL_REGEX.matches(email)) &&
            request.appVersion.isNotBlank() &&
            request.appVersion.length <= MAX_APP_VERSION_LENGTH &&
            request.locale.length <= MAX_LOCALE_LENGTH &&
            LOCALE_REGEX.matches(request.locale) &&
            request.platform == "android" &&
            request.createdAtEpochMillis > 0L &&
            request.optionalContext.clueRounds?.let { it in 1..3 } != false &&
            request.optionalContext.playerCount?.let { it in MIN_PLAYER_COUNT..MAX_PLAYER_COUNT } != false
    }
}
