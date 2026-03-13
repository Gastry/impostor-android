package com.impostorparty.domain.usecase

import com.impostorparty.domain.model.FeedbackValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateFeedbackInputUseCaseTest {

    private val useCase = ValidateFeedbackInputUseCase()

    @Test
    fun `message is required`() {
        val result = useCase(message = "   ", email = "")

        assertEquals(setOf(FeedbackValidationError.MESSAGE_REQUIRED), result)
    }

    @Test
    fun `message must have minimum length`() {
        val result = useCase(message = "short", email = "")

        assertEquals(setOf(FeedbackValidationError.MESSAGE_TOO_SHORT), result)
    }

    @Test
    fun `email stays optional when blank`() {
        val result = useCase(message = "Enough detail", email = " ")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invalid email is rejected`() {
        val result = useCase(message = "Enough detail", email = "mail@broken")

        assertEquals(setOf(FeedbackValidationError.EMAIL_INVALID), result)
    }

    @Test
    fun `valid payload passes validation`() {
        val result = useCase(message = "Enough detail", email = "mail@example.com")

        assertTrue(result.isEmpty())
    }
}
