package no.nav.tiltakspenger.vedtak.innsending

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.mars
import no.nav.tiltakspenger.domene.nå
import no.nav.tiltakspenger.objectmothers.ObjectMother.brukerTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedOvergangsstønad
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyUføreHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.uføreVedtak
import no.nav.tiltakspenger.vedtak.InnhentetUføre
import org.junit.jupiter.api.Test
import java.util.Random

internal class HåndterUføreHendelseTest {

    @Test
    fun `uføreVedtak med virkDato utenfor vurderingsperioden blir filtrert bort`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val innhentet = nå()

        val innsending = innsendingMedOvergangsstønad(
            ident = ident,
            journalpostId = journalpostId,
            søknad = nySøknadMedBrukerTiltak(
                tiltak = brukerTiltak(
                    startdato = 1.januar(2023),
                    sluttdato = 31.januar(2023),
                ),
            ),
        )

        innsending.håndter(
            nyUføreHendelse(
                ident = ident,
                journalpostId = journalpostId,
                uføreVedtak = uføreVedtak(
                    harUføregrad = true,
                    datoUfør = 15.januar(2023),
                    virkDato = 5.mars(2023),
                ),
                tidsstempelUføreVedtakInnhentet = innhentet,
            ),
        )

        innsending.uføreVedtak shouldBe InnhentetUføre(
            uføreVedtak = null,
            tidsstempelInnhentet = innhentet,
        )
        innsending.aktivitetslogg.aktiviteter()
            .map { it.melding } shouldContain "Filtrer bort vedtak da virkDato ikke er i vurderingsperioden Periode(range=[2023-01-01..2023-02-01))"
    }
}
