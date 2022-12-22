package no.nav.tiltakspenger.vedtak.service.innsending

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminServiceScheduler.Companion.TO_DØGN
import org.junit.jupiter.api.Test

internal class InnsendingAdminServiceSchedulerTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test scheduled task`() = runTest {

        val adminService = mockk<InnsendingAdminService>()
        val scheduler = InnsendingAdminServiceScheduler(adminService)
        every { adminService.resettInnsendingerSomErFerdigstilt() } returns Unit

        val returverdi = scheduler.scheduledTask()
        returverdi shouldBe true
        advanceTimeBy(TO_DØGN)
        advanceTimeBy(1000)
        verify { adminService.resettInnsendingerSomErFerdigstilt() }
    }
}
