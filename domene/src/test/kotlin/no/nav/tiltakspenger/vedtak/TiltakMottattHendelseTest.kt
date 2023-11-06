package no.nav.tiltakspenger.vedtak

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class TiltakMottattHendelseTest {
    @Test
    @Disabled("Denne håndteres annerledes nå, faktainnhenteren sender tom liste!")
    fun `lagre og hente en med feil i tiltak fra Arena`() {
        /*
        val journalpostId = Random().nextInt().toString()
        val innsending = innsendingMedSkjerming(journalpostId = journalpostId)

        innsending.håndter(
            nyTiltakHendelse(
                journalpostId = journalpostId,
                tiltaksaktivitet = null,
                feil = ArenaTiltakMottattHendelse.Feilmelding.PersonIkkeFunnet,
            )
        )
        innsending.aktivitetslogg.aktiviteter().forEach { println(it.melding) }
        innsending.aktivitetslogg.aktiviteter()
            .filter { it.melding == "Fant ikke person i arenetiltak" }.size shouldBe 1
        innsending.tilstand shouldBe Innsending.FaktainnhentingFeilet
         */
    }
}
