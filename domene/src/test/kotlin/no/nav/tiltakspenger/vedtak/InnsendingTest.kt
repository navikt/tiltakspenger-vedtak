package no.nav.tiltakspenger.vedtak

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.innsendingMedSøknad
import no.nav.tiltakspenger.objectmothers.innsendingRegistrert
import no.nav.tiltakspenger.objectmothers.nyPersonopplysningHendelse
import no.nav.tiltakspenger.objectmothers.nySøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import org.junit.jupiter.api.Test

internal class InnsendingTest {

    @Test
    fun `å motta samme søknad to ganger resulterer i Info i aktivitetsloggen og ingen behov`() {

        val innsending = innsendingMedSøknad()
        val søknad = innsending.søknad
        val sizeBefore = innsending.aktivitetslogg.aktiviteter().size
        val behovCountBefore = innsending.aktivitetslogg.aktiviteter().filter { it.alvorlighetsgrad == 50 }.size

        innsending.håndter(
            SøknadMottattHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = innsending.journalpostId,
                søknad = søknad!!,
            )
        )

        innsending.aktivitetslogg.aktiviteter().size - sizeBefore shouldBe 2
        innsending.aktivitetslogg.aktiviteter().map { it.melding } shouldContain "Registrert SøknadMottattHendelse"
        innsending.aktivitetslogg.aktiviteter()
            .map { it.melding } shouldContain "Forventet ikke SøknadMottattHendelse i AvventerPersonopplysninger"
        innsending.aktivitetslogg.aktiviteter().map { it.alvorlighetsgrad } shouldContain 25
        innsending.aktivitetslogg.aktiviteter().map { it.label } shouldContain 'W'
        innsending.aktivitetslogg.aktiviteter().map { it.kontekster }
            .map { it.map { kontekst -> kontekst.melding() } } shouldContain listOf(
            "SøknadMottattHendelse - journalpostId: ${innsending.journalpostId}",
            "Innsending - journalpostId: ${innsending.journalpostId}",
            "Tilstand - tilstandtype: AvventerPersonopplysninger"
        )
        innsending.aktivitetslogg.aktiviteter().filter { it.alvorlighetsgrad == 50 }.size - behovCountBefore shouldBe 0
        innsending.isDirty() shouldBe true
    }

    @Test
    fun `Innsending should be dirty when changed`() {

        val innsending1 = innsendingRegistrert(journalpostId = "1")
        // Hendelsen medfører ingen endringer annet enn i aktivitetsloggen
        innsending1.håndter(nyPersonopplysningHendelse(journalpostId = "1"))
        innsending1.isDirty() shouldBe true

        val innsending2 = innsendingRegistrert(journalpostId = "1")
        // Søknad blir lagret på Innsendingen
        innsending2.håndter(nySøknadMottattHendelse(journalpostId = "1"))
        innsending2.isDirty() shouldBe true
    }
}
