package no.nav.tiltakspenger.vedtak.db

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepoTest.Companion.random
import java.time.LocalDate

internal fun TestDataHelper.persisterSøknad(
    ident: String = random.nextInt().toString(),
    deltakelseFom: LocalDate = 1.januar(2023),
    deltakelseTom: LocalDate = 31.mars(2023),
    journalpostId: String = random.nextInt().toString(),
    søknad: Søknad = ObjectMother.nySøknad(
        journalpostId = journalpostId,
        personopplysninger = ObjectMother.personSøknad(
            ident = ident,
        ),
        tiltak = ObjectMother.søknadTiltak(
            deltakelseFom = deltakelseFom,
            deltakelseTom = deltakelseTom,
        ),
        barnetillegg = listOf(ObjectMother.barnetilleggMedIdent()),
    ),

): Søknad {
    this.søknadRepo.lagre(søknad)
    return søknadRepo.hentSøknad(søknad.id).also {
        it shouldBe søknad
    }
}
