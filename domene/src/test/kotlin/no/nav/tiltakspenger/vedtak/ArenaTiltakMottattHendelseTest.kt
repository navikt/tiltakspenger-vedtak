package no.nav.tiltakspenger.vedtak

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.innsendingMedSkjerming
import no.nav.tiltakspenger.objectmothers.nyTiltakHendelse
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import org.junit.jupiter.api.Test
import java.util.*

class ArenaTiltakMottattHendelseTest {
    @Test
    fun `lagre og hente en med feil i tiltak fra Arena`() {
        val journalpostId = Random().nextInt().toString()
        val innsending = innsendingMedSkjerming(journalpostId = journalpostId)

        innsending.håndter(
            nyTiltakHendelse(
                journalpostId = journalpostId,
                tiltaksaktivitet = null,
                feil = ArenaTiltakMottattHendelse.Feilmelding.PersonIkkeFunnet,
            )
        )

        innsending.aktivitetslogg.aktiviteter.filter { it.melding == "Fant ikke person i arenetiltak" }.size shouldBe 1
        innsending.tilstand shouldBe Innsending.FaktainnhentingFeilet
    }
}
