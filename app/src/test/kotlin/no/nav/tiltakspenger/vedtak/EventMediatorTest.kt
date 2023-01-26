package no.nav.tiltakspenger.vedtak

import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tiltakspenger.vedtak.routes.rivers.DayHasBegunEvent
import no.nav.tiltakspenger.vedtak.service.innsending.InnsendingAdminService
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EventMediatorTest {

    private val rapidsConnection = TestRapid()
    private val innsendingAdminService = mockk<InnsendingAdminService>()

    @Test
    fun `skal sende ut en event per journalpostid`() {
        every { innsendingAdminService.hentInnsendingerSomErFerdigstilt() } returns listOf("1", "2", "3")

        val eventMediator =
            EventMediator(rapidsConnection = rapidsConnection, innsendingAdminService = innsendingAdminService)
        eventMediator.håndter(DayHasBegunEvent(LocalDate.of(2022, 10, 1)))

        with(rapidsConnection.inspektør) {
            size shouldBeExactly 3
            field(0, "@event_name").asText() shouldBe "InnsendingUtdatertHendelse"
            field(0, "dag").asText() shouldBe "2022-10-01"
            field(0, "journalpostId").asText() shouldBe "1"
            field(1, "journalpostId").asText() shouldBe "2"
            field(2, "journalpostId").asText() shouldBe "3"
        }
    }
}
