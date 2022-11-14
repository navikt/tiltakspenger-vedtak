package no.nav.tiltakspenger.vedtak

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.innsendingMedSøknad
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import org.junit.jupiter.api.Test

internal class InnsendingTest {

    @Test
    fun `å motta samme søknad to ganger resulterer i Info i aktivitetsloggen og ingen behov`() {

        val innsending = innsendingMedSøknad()
        val søknad = innsending.søknad
        innsending.aktivitetslogg.aktiviteter.clear()

        innsending.håndter(
            SøknadMottattHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = innsending.journalpostId,
                søknad = søknad!!,
            )
        )

        innsending.aktivitetslogg.aktiviteter.size shouldBe 2
        innsending.aktivitetslogg.aktiviteter.first().melding shouldBe "Registrert SøknadMottattHendelse"
        innsending.aktivitetslogg.aktiviteter.last().melding shouldBe "søknad ${søknad.søknadId} er motttatt og lagret tidligere"
        innsending.aktivitetslogg.aktiviteter.last().alvorlighetsgrad shouldBe 0
        innsending.aktivitetslogg.aktiviteter.last().label shouldBe 'I'
        innsending.aktivitetslogg.aktiviteter.last().kontekster.map { it.melding() } shouldContainExactly listOf(
            "SøknadMottattHendelse - ident: ${innsending.ident}",
            "Søker - ident: ${innsending.ident}",
            "Tilstand - tilstandtype: AvventerPersonopplysninger"
        )
        innsending.aktivitetslogg.aktiviteter.filter { it.alvorlighetsgrad == 50 }.size shouldBe 0
    }
}
