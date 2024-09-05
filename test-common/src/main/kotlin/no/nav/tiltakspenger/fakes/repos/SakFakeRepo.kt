package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import arrow.core.nonEmptyListOf
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.sak.TynnSak
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import java.time.LocalDate

class SakFakeRepo(
    private val personopplysningerRepo: PersonopplysningerFakeRepo,
    private val behandlingRepo: BehandlingFakeRepo,
    private val vedtakRepo: RammevedtakFakeRepo,
    private val meldekortRepo: MeldekortFakeRepo,
) : SakRepo {

    // TODO jah: Bør heller komponere denne basert på
    val data = Atomic(mutableMapOf<SakId, Sak>())

    override fun hentForFnr(fnr: Fnr): Saker {
        return Saker(fnr, data.get().values.filter { it.fnr == fnr })
    }

    override fun hentForSaksnummer(saksnummer: Saksnummer): Sak? {
        return data.get().values.find { it.saksnummer == saksnummer }
    }

    override fun lagre(sak: Sak, transactionContext: TransactionContext?): Sak {
        return sak.also { data.get()[sak.id] = sak }
    }

    override fun hentForSakId(sakId: SakId): Sak? {
        val førstegangsbehandling = behandlingRepo.hentFørstegangsbehandlingForSakId(sakId)
        val personopplysninger = personopplysningerRepo.hent(sakId)
        val vedtak = vedtakRepo.hentForSakId(sakId)
        val meldekort = meldekortRepo.hentForSakId(sakId)
        if (førstegangsbehandling == null || vedtak == null) {
            return null
        }
        return Sak(
            sakDetaljer = TynnSak(
                id = sakId,
                fnr = førstegangsbehandling.fnr,
                saksnummer = førstegangsbehandling.saksnummer,
            ),
            behandlinger = nonEmptyListOf(førstegangsbehandling),
            personopplysninger = personopplysninger,
            vedtak = listOf(vedtak),
            meldekort = meldekort!!,
        )
    }

    override fun hentDetaljerForSakId(sakId: SakId): SakDetaljer? {
        return data.get()[sakId]?.let {
            TynnSak(
                id = it.id,
                fnr = it.fnr,
                saksnummer = it.saksnummer,
            )
        }
    }

    override fun hentNesteSaksnummer(): Saksnummer {
        return data.get().values.map { it.saksnummer }.lastOrNull()?.nesteSaksnummer() ?: Saksnummer.genererSaknummer(dato = LocalDate.now())
    }

    override fun hentFnrForSakId(sakId: SakId, sessionContext: SessionContext?): Fnr? {
        return data.get()[sakId]?.fnr
    }

    override fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): Sak? {
        return data.get().values.find { it.behandlinger.any { behandling -> behandling.id == behandlingId } }
    }

    override fun hentForSøknadId(søknadId: SøknadId): Sak? {
        return data.get().values.find { it.behandlinger.any { behandling -> behandling.søknad.id == søknadId } }
    }
}
