package no.nav.tiltakspenger.vedtak

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.søkerMedSøknad
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import org.junit.jupiter.api.Test

internal class SøkerTest {

    @Test
    fun `å motta samme søknad to ganger resulterer i Info i aktivitetsloggen og ingen behov`() {

        val søker = søkerMedSøknad()
        val søknad = søker.søknader.first()
        søker.aktivitetslogg.aktiviteter.clear()

        søker.håndter(
            SøknadMottattHendelse(
                aktivitetslogg = Aktivitetslogg(),
                ident = søker.ident,
                søknad = søknad,
            )
        )

        søker.aktivitetslogg.aktiviteter.size shouldBe 2
        søker.aktivitetslogg.aktiviteter.first().melding shouldBe "Registrert SøknadMottattHendelse"
        søker.aktivitetslogg.aktiviteter.last().melding shouldBe "søknad ${søknad.søknadId} er motttatt og lagret tidligere"
        søker.aktivitetslogg.aktiviteter.last().alvorlighetsgrad shouldBe 0
        søker.aktivitetslogg.aktiviteter.last().label shouldBe 'I'
        søker.aktivitetslogg.aktiviteter.last().kontekster.map { it.melding() } shouldContainExactly listOf(
            "SøknadMottattHendelse - ident: ${søker.ident}",
            "Søker - ident: ${søker.ident}",
            "Tilstand - tilstandtype: AvventerPersonopplysninger"
        )
        søker.aktivitetslogg.aktiviteter.filter { it.alvorlighetsgrad == 50 }.size shouldBe 0
    }
}
