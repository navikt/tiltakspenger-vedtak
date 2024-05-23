package no.nav.tiltakspenger.innsending

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.innsending.domene.InnhentetUføre
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedOvergangsstønad
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyUføreHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.uføreVedtak
import org.junit.jupiter.api.Test
import java.util.Random

internal class HåndterUføreHendelseTest {

    @Test
    fun `uføreVedtak med virkDato utenfor vurderingsperioden blir filtrert bort`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val innhentet = nå()

        val innsending = innsendingMedOvergangsstønad(
            fom = 1.januar(2023),
            tom = 31.januar(2023),
            ident = ident,
            journalpostId = journalpostId,
            søknad = nySøknad(
                tiltak = søknadTiltak(
                    deltakelseFom = 1.januar(2023),
                    deltakelseTom = 31.januar(2023),
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
            .map { it.melding } shouldContain "Filtrer bort vedtak da virkDato ikke er i vurderingsperioden Periode(fra=2023-01-01 til=2023-02-01)"
    }
}
