package com.impostorparty.app.viewmodel

import com.impostorparty.domain.model.FeedbackSendResult
import com.impostorparty.domain.model.FeedbackType
import com.impostorparty.domain.usecase.SendFeedbackUseCase
import com.impostorparty.domain.usecase.ValidateFeedbackInputUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var feedbackRepository: FakeFeedbackRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        preferencesRepository = FakePreferencesRepository()
        feedbackRepository = FakeFeedbackRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): FeedbackViewModel {
        return FeedbackViewModel(
            preferencesRepository = preferencesRepository,
            validateFeedbackInputUseCase = ValidateFeedbackInputUseCase(),
            sendFeedbackUseCase = SendFeedbackUseCase(feedbackRepository),
        )
    }

    @Test
    fun `feedback requires message`() = runTest(dispatcher) {
        val viewModel = buildViewModel()

        viewModel.updateFeedbackMessage(" ")
        viewModel.submitFeedback(null)
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.validationErrors.isNotEmpty())
        assertEquals(0, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `feedback rejects invalid optional email`() = runTest(dispatcher) {
        val viewModel = buildViewModel()

        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.updateFeedbackEmail("broken-email")
        viewModel.submitFeedback(null)
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.validationErrors.isNotEmpty())
        assertEquals(0, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `feedback submit success updates ui state`() = runTest(dispatcher) {
        feedbackRepository.enqueueResult(FeedbackSendResult.Success)
        val viewModel = buildViewModel()

        viewModel.updateFeedbackType(FeedbackType.PROBLEM)
        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.updateFeedbackEmail("mail@example.com")
        viewModel.submitFeedback(FeedbackContextHint(clueRounds = 2, playerCount = 6))
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.isSuccess)
        assertEquals(1, feedbackRepository.sentRequests.size)
        assertEquals(2, feedbackRepository.sentRequests.single().context.clueRounds)
    }

    @Test
    fun `feedback submit network error can retry`() = runTest(dispatcher) {
        feedbackRepository.enqueueResult(FeedbackSendResult.NetworkError)
        feedbackRepository.enqueueResult(FeedbackSendResult.Success)
        val viewModel = buildViewModel()

        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.submitFeedback(null)
        advanceUntilIdle()

        assertEquals(FeedbackSendResult.NetworkError, viewModel.feedbackForm.value.sendResult)

        viewModel.retryFeedbackSubmission(null)
        advanceUntilIdle()

        assertTrue(viewModel.feedbackForm.value.isSuccess)
        assertEquals(2, feedbackRepository.sentRequests.size)
    }

    @Test
    fun `clear feedback status keeps draft and clears transient state`() = runTest(dispatcher) {
        val viewModel = buildViewModel()

        viewModel.updateFeedbackType(FeedbackType.PROBLEM)
        viewModel.updateFeedbackMessage("Enough detail here")
        viewModel.updateFeedbackEmail("bad-email")
        viewModel.submitFeedback(null)
        advanceUntilIdle()

        viewModel.clearFeedbackStatus()

        assertEquals(FeedbackType.PROBLEM, viewModel.feedbackForm.value.type)
        assertEquals("Enough detail here", viewModel.feedbackForm.value.message)
        assertEquals("bad-email", viewModel.feedbackForm.value.email)
        assertNull(viewModel.feedbackForm.value.sendResult)
        assertTrue(viewModel.feedbackForm.value.validationErrors.isEmpty())
        assertTrue(!viewModel.feedbackForm.value.isSuccess)
    }
}
