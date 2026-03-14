package com.impostorparty.data.repository

import com.impostorparty.domain.model.FeedbackContext
import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission
import com.impostorparty.domain.model.FeedbackType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppsScriptFeedbackRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `missing token configuration is rejected before network`() = runBlocking {
        val repository = AppsScriptFeedbackRepository(
            json = json,
            endpoint = "https://example.com/feedback",
            sharedToken = "",
            openConnection = { error("Network should not be called") },
        )

        val result = repository.sendFeedback(validSubmission())

        assertEquals(FeedbackSendResult.MissingConfiguration, result)
    }

    @Test
    fun `invalid request payload is rejected before network`() = runBlocking {
        val repository = AppsScriptFeedbackRepository(
            json = json,
            endpoint = "https://example.com/feedback",
            sharedToken = "1234567890abcdef",
            openConnection = { error("Network should not be called") },
        )

        val result = repository.sendFeedback(
            validSubmission().copy(context = validSubmission().context.copy(locale = "")),
        )

        assertEquals(FeedbackSendResult.InvalidRequest, result)
    }

    @Test
    fun `valid request sends token and succeeds`() = runBlocking {
        lateinit var connection: FakeHttpURLConnection
        val repository = AppsScriptFeedbackRepository(
            json = json,
            endpoint = "https://example.com/feedback",
            sharedToken = "1234567890abcdef",
            openConnection = {
                FakeHttpURLConnection(URL(it), """{"ok":true}""").also { created -> connection = created }
            },
        )

        val result = repository.sendFeedback(validSubmission())

        assertEquals(FeedbackSendResult.Success, result)
        assertTrue(connection.outputUtf8.contains("\"token\":\"1234567890abcdef\""))
        assertEquals("application/json; charset=utf-8", connection.requestPropertiesMap["Content-Type"])
    }

    private fun validSubmission(): FeedbackSubmission {
        return FeedbackSubmission(
            type = FeedbackType.SUGGESTION,
            message = "Enough detail for the request",
            email = "mail@example.com",
            context = FeedbackContext(
                appVersion = "1.0.0",
                locale = "es",
                timestampEpochMillis = 1_700_000_000_000,
                clueRounds = 2,
                playerCount = 6,
            ),
        )
    }
}

private class FakeHttpURLConnection(
    url: URL,
    responseBody: String,
) : HttpURLConnection(url) {

    private val inputBytes = responseBody.toByteArray(Charsets.UTF_8)
    private val output = ByteArrayOutputStream()
    val requestPropertiesMap = linkedMapOf<String, String>()
    val outputUtf8: String
        get() = output.toString(Charsets.UTF_8.name())

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun setRequestProperty(key: String, value: String) {
        requestPropertiesMap[key] = value
    }

    override fun getOutputStream(): OutputStream = output

    override fun getInputStream(): InputStream = ByteArrayInputStream(inputBytes)

    override fun getResponseCode(): Int = HTTP_OK
}
