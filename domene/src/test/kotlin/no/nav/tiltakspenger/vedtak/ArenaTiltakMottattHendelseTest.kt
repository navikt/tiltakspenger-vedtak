package no.nav.tiltakspenger.vedtak

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.nyTiltakHendelse
import no.nav.tiltakspenger.objectmothers.søkerMedSkjerming
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import org.junit.jupiter.api.Test
import java.util.*

class ArenaTiltakMottattHendelseTest {
    @Test
    fun `lagre og hente en med feil i tiltak fra Arena`() {
        val ident = Random().nextInt().toString()
        val søker = søkerMedSkjerming(ident = ident)

        søker.håndter(
            nyTiltakHendelse(
                ident = ident,
                tiltaksaktivitet = null,
                feil = ArenaTiltakMottattHendelse.Feilmelding.PersonIkkeFunnet,
            )
        )

        søker.aktivitetslogg.aktiviteter.filter { it.melding == "Fant ikke person i arenetiltak" }.size shouldBe 1
        søker.tilstand shouldBe Søker.FaktainnhentingFeilet
    }
}
