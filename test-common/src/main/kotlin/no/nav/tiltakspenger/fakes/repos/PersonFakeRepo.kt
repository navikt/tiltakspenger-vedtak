package no.nav.tiltakspenger.fakes.repos

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.PersonRepo

class PersonFakeRepo(
    private val sakFakeRepo: SakFakeRepo,
) : PersonRepo {
    override fun hentFnrForSakId(sakId: SakId): Fnr? {
        return sakFakeRepo.data.get()[sakId]!!.fnr
    }

    override fun hentFnrForBehandlingId(behandlingId: BehandlingId): Fnr? {
        return sakFakeRepo.data.get().values.find { it.behandlinger.any { it.id == behandlingId } }?.fnr
    }

    override fun hentFnrForSaksnummer(saksnummer: Saksnummer): Fnr? {
        return sakFakeRepo.data.get().values.find { it.saksnummer == saksnummer }?.fnr
    }

    override fun hentFnrForVedtakId(vedtakId: VedtakId): Fnr? {
        return sakFakeRepo.data.get().values.find { it.rammevedtak!!.id == vedtakId }?.fnr
    }

    override fun hentFnrForMeldekortId(meldekortId: MeldekortId): Fnr? {
        return sakFakeRepo.data.get().values.find { it.meldeperioder.any { it.id == meldekortId } }?.fnr
    }

    override fun hentFnrForSøknadId(søknadId: SøknadId): Fnr? {
        return sakFakeRepo.data.get().values.find { it.førstegangsbehandling.søknad.id == søknadId }?.fnr
    }
}
