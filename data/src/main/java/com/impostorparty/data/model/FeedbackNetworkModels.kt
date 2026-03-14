package com.impostorparty.data.model

import com.impostorparty.domain.model.FeedbackSubmission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackApiRequest(
    val type: String,
    val message: String,
    val email: String? = null,
    val token: String,
    @SerialName("appVersion") val appVersion: String,
    val locale: String,
    val platform: String,
    @SerialName("createdAt") val createdAtEpochMillis: Long,
    @SerialName("optionalContext") val optionalContext: FeedbackOptionalContext,
)

@Serializable
data class FeedbackOptionalContext(
    val clueRounds: Int? = null,
    val playerCount: Int? = null,
)

@Serializable
data class FeedbackApiResponse(
    val ok: Boolean,
    val error: String? = null,
)

fun FeedbackSubmission.toApiRequest(sharedToken: String): FeedbackApiRequest = FeedbackApiRequest(
    type = type.name.lowercase(),
    message = message.trim(),
    email = email?.trim()?.ifBlank { null },
    token = sharedToken.trim(),
    appVersion = context.appVersion,
    locale = context.locale,
    platform = context.platform,
    createdAtEpochMillis = context.timestampEpochMillis,
    optionalContext = FeedbackOptionalContext(
        clueRounds = context.clueRounds,
        playerCount = context.playerCount,
    ),
)
