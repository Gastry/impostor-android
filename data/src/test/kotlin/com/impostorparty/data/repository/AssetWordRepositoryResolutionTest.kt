package com.impostorparty.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class AssetWordRepositoryResolutionTest {

    @Test
    fun `latin american spanish prefers es 419 over generic es`() {
        val resolved = regionAwareCandidates(
            requestedTag = "es-MX",
            available = setOf("en", "es", "es-es", "es-419"),
        )

        assertEquals(listOf("es-mx", "es-419", "es"), resolved)
    }

    @Test
    fun `spain spanish prefers es es over generic es`() {
        val resolved = regionAwareCandidates(
            requestedTag = "es-ES",
            available = setOf("en", "es", "es-es", "es-419"),
        )

        assertEquals(listOf("es-es", "es"), resolved)
    }

    @Test
    fun `generic spanish keeps generic es only`() {
        val resolved = regionAwareCandidates(
            requestedTag = "es",
            available = setOf("en", "es", "es-es", "es-419"),
        )

        assertEquals(listOf("es"), resolved)
    }
}
