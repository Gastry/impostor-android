package com.impostorparty.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostorparty.app.BuildConfig
import com.impostorparty.domain.model.FeedbackContext
import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackSubmission
import com.impostorparty.domain.model.FeedbackType
import com.impostorparty.domain.model.FeedbackValidationError
import com.impostorparty.domain.repository.PreferencesRepository
import com.impostorparty.domain.usecase.SendFeedbackUseCase
import com.impostorparty.domain.usecase.ValidateFeedbackInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val validateFeedbackInputUseCase: ValidateFeedbackInputUseCase,
    private val sendFeedbackUseCase: SendFeedbackUseCase,
) : ViewModel() {

    private val _feedbackForm = MutableStateFlow(FeedbackFormUiState())
    val feedbackForm: StateFlow<FeedbackFormUiState> = _feedbackForm.asStateFlow()

    fun updateFeedbackType(type: FeedbackType) {
        _feedbackForm.update { it.copy(type = type, isSuccess = false, sendResult = null) }
    }

    fun updateFeedbackMessage(message: String) {
        _feedbackForm.update {
            it.copy(
                message = message,
                validationErrors = it.validationErrors - setOf(
                    FeedbackValidationError.MESSAGE_REQUIRED,
                    FeedbackValidationError.MESSAGE_TOO_SHORT,
                ),
                isSuccess = false,
                sendResult = null,
            )
        }
    }

    fun updateFeedbackEmail(email: String) {
        _feedbackForm.update {
            it.copy(
                email = email,
                validationErrors = it.validationErrors - FeedbackValidationError.EMAIL_INVALID,
                isSuccess = false,
                sendResult = null,
            )
        }
    }

    fun submitFeedback(contextHint: FeedbackContextHint?) {
        val current = _feedbackForm.value
        if (current.isSending) return

        val validationErrors = validateFeedbackInputUseCase(current.message, current.email)
        if (validationErrors.isNotEmpty()) {
            _feedbackForm.update { it.copy(validationErrors = validationErrors, isSuccess = false, sendResult = null) }
            return
        }

        _feedbackForm.value = current.copy(
            isSending = true,
            validationErrors = emptySet(),
            isSuccess = false,
            sendResult = null,
        )

        viewModelScope.launch {
            val settingsSnapshot = preferencesRepository.appSettings.first()
            val localeTag = resolveEffectiveLanguageTag(settingsSnapshot.languageTag)

            val feedbackContext = FeedbackContext(
                appVersion = BuildConfig.VERSION_NAME,
                locale = localeTag,
                timestampEpochMillis = System.currentTimeMillis(),
                clueRounds = contextHint?.clueRounds,
                playerCount = contextHint?.playerCount,
            )

            val result = sendFeedbackUseCase(
                FeedbackSubmission(
                    type = current.type,
                    message = current.message.trim(),
                    email = current.email.trim().ifBlank { null },
                    context = feedbackContext,
                ),
            )

            _feedbackForm.update { state ->
                if (result == FeedbackSendResult.Success) {
                    FeedbackFormUiState(
                        type = state.type,
                        isSuccess = true,
                    )
                } else {
                    state.copy(
                        isSending = false,
                        sendResult = result,
                    )
                }
            }
        }
    }

    fun retryFeedbackSubmission(contextHint: FeedbackContextHint?) {
        submitFeedback(contextHint)
    }

    fun clearFeedbackStatus() {
        _feedbackForm.update { it.copy(isSuccess = false, sendResult = null, validationErrors = emptySet()) }
    }

    private fun resolveEffectiveLanguageTag(preferredLanguageTag: String?): String {
        return preferredLanguageTag
            ?.ifBlank { null }
            ?: Locale.getDefault().toLanguageTag().ifBlank { "en" }
    }
}
