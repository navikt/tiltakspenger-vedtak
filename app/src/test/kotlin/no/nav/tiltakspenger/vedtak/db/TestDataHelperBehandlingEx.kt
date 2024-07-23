package no.nav.tiltakspenger.vedtak.db

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
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
    saksnummer: Saksnummer = Saksnummer("202301011001"),
    sakensPeriode: Periode = Periode(fraOgMed = deltakelseFom, tilOgMed = deltakelseTom),
    sakPersonopplysninger: SakPersonopplysninger = SakPersonopplysninger(),
): Triple<Sak, Søknad, Førstegangsbehandling> {
    val søknad = this.persisterSøknad(
        ident = ident,
        deltakelseFom = deltakelseFom,
        deltakelseTom = deltakelseTom,
        journalpostId = journalpostId,
    )
    val sak = Sak(
        id = sakId,
        ident = ident,
        saknummer = saksnummer,
        periode = sakensPeriode,
        behandlinger = listOf(),
        personopplysninger = sakPersonopplysninger,
        vedtak = listOf(),
    )
    sakRepo.lagre(sak)

    val behandling = ObjectMother.sakMedOpprettetBehandling(
        sakId = sakId,
        saksbehandler = saksbehandler,
        søknad = søknad,
    ).førstegangsbehandling!!

    behandlingRepo.lagre(behandling)
    return Triple(
        sakRepo.hent(sakId)!!,
        søknadRepo.hentSøknad(søknad.id),
        behandlingRepo.hent(behandling.id),
    )
}
