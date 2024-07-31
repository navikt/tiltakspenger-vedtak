package no.nav.tiltakspenger.vedtak.db

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepoTest.Companion.random
import java.time.LocalDate

internal fun TestDataHelper.persisterOpprettetFørstegangsbehandling(
    sakId: SakId = SakId.random(),
    ident: String = random.nextInt().toString(),
    deltakelseFom: LocalDate = 1.januar(2023),
    deltakelseTom: LocalDate = 31.mars(2023),
    journalpostId: String = random.nextInt().toString(),
    saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    iDag: LocalDate = LocalDate.of(2023, 1, 1),
    løpenummer: Int = 1001,
    saksnummer: Saksnummer = Saksnummer(iDag, løpenummer),
    tiltaksOgVurderingsperiode: Periode = Periode(fraOgMed = deltakelseFom, tilOgMed = deltakelseTom),
    fødselsdato: LocalDate = 1.januar(2001),
    sakPersonopplysninger: SakPersonopplysninger = SakPersonopplysninger(listOf(personopplysningKjedeligFyr(ident = ident, fødselsdato = fødselsdato))),
    id: SøknadId = Søknad.randomId(),
    søknad: Søknad = ObjectMother.nySøknad(
        periode = tiltaksOgVurderingsperiode,
        journalpostId = journalpostId,
        personopplysninger = ObjectMother.personSøknad(
            ident = ident,
        ),
        id = id,
        tiltak = ObjectMother.søknadTiltak(
            deltakelseFom = deltakelseFom,
            deltakelseTom = deltakelseTom,
        ),
        barnetillegg = listOf(ObjectMother.barnetilleggMedIdent()),
    ),
): Pair<Sak, Søknad> {
    this.persisterSøknad(
        søknad = søknad,
    )
    val sak = ObjectMother.sakMedOpprettetBehandling(
        søknad = søknad,
        ident = ident,
        vurderingsperiode = tiltaksOgVurderingsperiode,
        saksnummer = saksnummer,
        sakPersonopplysninger = sakPersonopplysninger,
        saksbehandler = saksbehandler,
        sakId = sakId,
    )
    søknadRepo.lagre(søknad)
    sakRepo.lagre(sak)

    return Pair(
        sakRepo.hent(sakId)!!,
        søknadRepo.hentSøknad(søknad.id),
    )
}
