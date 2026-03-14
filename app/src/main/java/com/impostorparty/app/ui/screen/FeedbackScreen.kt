package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PrimaryPartyButton
import com.impostorparty.app.ui.components.SecondaryPartyButton
import com.impostorparty.app.ui.theme.PartyDimens
import com.impostorparty.app.viewmodel.FeedbackFormUiState
import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackType
import com.impostorparty.domain.model.FeedbackValidationError

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackScreen(
    state: FeedbackFormUiState,
    onBack: () -> Unit,
    onTypeChanged: (FeedbackType) -> Unit,
    onMessageChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
) {
    PartyScaffold(
        title = stringResource(R.string.feedback_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                )
            }
        },
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = PartyDimens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
        ) {
            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                    Text(
                        text = stringResource(R.string.feedback_form_intro),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.feedback_form_privacy),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                    Text(
                        text = stringResource(R.string.feedback_type_label),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                        verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm),
                    ) {
                        FeedbackType.entries.forEach { type ->
                            FilterChip(
                                selected = state.type == type,
                                onClick = { onTypeChanged(type) },
                                label = { Text(stringResource(typeLabel(type))) },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.message,
                        onValueChange = onMessageChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_message"),
                        minLines = 5,
                        label = { Text(stringResource(R.string.feedback_message_label)) },
                        placeholder = { Text(stringResource(R.string.feedback_message_placeholder)) },
                        supportingText = {
                            val validationMessage = messageValidationText(state.validationErrors)
                            if (validationMessage != null) {
                                Text(text = validationMessage, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text(text = stringResource(R.string.feedback_message_help))
                            }
                        },
                        isError = state.validationErrors.any {
                            it == FeedbackValidationError.MESSAGE_REQUIRED ||
                                it == FeedbackValidationError.MESSAGE_TOO_SHORT ||
                                it == FeedbackValidationError.MESSAGE_TOO_LONG
                        },
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_email"),
                        singleLine = true,
                        label = { Text(stringResource(R.string.feedback_email_label)) },
                        placeholder = { Text(stringResource(R.string.feedback_email_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        supportingText = {
                            if (FeedbackValidationError.EMAIL_INVALID in state.validationErrors) {
                                Text(
                                    text = stringResource(R.string.feedback_email_error),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text(text = stringResource(R.string.feedback_email_help))
                            }
                        },
                        isError = FeedbackValidationError.EMAIL_INVALID in state.validationErrors,
                    )
                }
            }

            PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceXs)) {
                    Text(
                        text = stringResource(R.string.feedback_context_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.feedback_context_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.isSuccess) {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.feedback_success),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("feedback_success"),
                    )
                }
            }

            val sendError = sendErrorText(state.sendResult)
            if (sendError != null) {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceSm)) {
                        Text(
                            text = sendError,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("feedback_error"),
                        )
                        SecondaryPartyButton(
                            text = stringResource(R.string.feedback_retry),
                            onClick = onRetry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feedback_retry"),
                        )
                    }
                }
            }

            PrimaryPartyButton(
                text = if (state.isSending) {
                    stringResource(R.string.feedback_sending)
                } else {
                    stringResource(R.string.feedback_send)
                },
                onClick = onSubmit,
                enabled = !state.isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_send"),
            )
        }
    }
}

@Composable
private fun messageValidationText(errors: Set<FeedbackValidationError>): String? {
    return when {
        FeedbackValidationError.MESSAGE_REQUIRED in errors -> stringResource(R.string.feedback_message_error_required)
        FeedbackValidationError.MESSAGE_TOO_SHORT in errors -> stringResource(R.string.feedback_message_error_short)
        FeedbackValidationError.MESSAGE_TOO_LONG in errors -> stringResource(R.string.feedback_message_error_long)
        else -> null
    }
}

@Composable
private fun sendErrorText(result: FeedbackSendResult?): String? {
    return when (result) {
        FeedbackSendResult.MissingConfiguration -> stringResource(R.string.feedback_error_missing_endpoint)
        FeedbackSendResult.InvalidRequest -> stringResource(R.string.feedback_error_server)
        FeedbackSendResult.NetworkError -> stringResource(R.string.feedback_error_network)
        FeedbackSendResult.ServerError -> stringResource(R.string.feedback_error_server)
        FeedbackSendResult.InvalidResponse -> stringResource(R.string.feedback_error_invalid_response)
        FeedbackSendResult.Success, null -> null
    }
}

private fun typeLabel(type: FeedbackType): Int {
    return when (type) {
        FeedbackType.SUGGESTION -> R.string.feedback_type_suggestion
        FeedbackType.PROBLEM -> R.string.feedback_type_problem
    }
}
