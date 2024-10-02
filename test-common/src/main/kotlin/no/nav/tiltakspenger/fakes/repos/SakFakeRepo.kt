package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import arrow.core.nonEmptyListOf
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
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
    private val rammevedtakRepo: RammevedtakFakeRepo,
    private val meldekortRepo: MeldekortFakeRepo,
) : SakRepo {
    val data = Atomic(mutableMapOf<SakId, Sak>())

    override fun hentForFnr(fnr: Fnr): Saker = Saker(fnr, data.get().values.filter { it.fnr == fnr })

    override fun hentForSaksnummer(saksnummer: Saksnummer): Sak? =
        data.get().values.find { it.saksnummer == saksnummer }

    override fun lagre(
        sak: Sak,
        transactionContext: TransactionContext?,
    ) {
        data.get()[sak.id] = sak
        personopplysningerRepo.lagre(sak.id, sak.personopplysninger)
        behandlingRepo.lagre(sak.førstegangsbehandling)
    }

    override fun hentForSakId(sakId: SakId): Sak? {
        val behandlinger = nonEmptyListOf(behandlingRepo.hentFørstegangsbehandlingForSakId(sakId)!!)
        return data.get()[sakId]?.copy(
            personopplysninger = personopplysningerRepo.hent(sakId),
            behandlinger = behandlinger,
            vedtak = rammevedtakRepo.hentForSakId(sakId)?.let { listOf(it) } ?: emptyList(),
            meldekort = meldekortRepo.hentForSakId(sakId) ?: Meldeperioder.empty(behandlinger.first().tiltakstype),
        )
    }

    override fun hentDetaljerForSakId(sakId: SakId): SakDetaljer? =
        data.get()[sakId]?.let {
            TynnSak(
                id = it.id,
                fnr = it.fnr,
                saksnummer = it.saksnummer,
            )
        }

    override fun hentNesteSaksnummer(): Saksnummer =
        data
            .get()
            .values
            .map { it.saksnummer }
            .lastOrNull()
            ?.nesteSaksnummer()
            ?: Saksnummer.genererSaknummer(dato = LocalDate.now())

    override fun hentFnrForSaksnummer(saksnummer: Saksnummer, sessionContext: SessionContext?): Fnr? {
        return data.get().values.singleOrNull { it.saksnummer == saksnummer }?.fnr
    }

    override fun hentFnrForSakId(
        sakId: SakId,
        sessionContext: SessionContext?,
    ): Fnr? = data.get()[sakId]?.fnr

    override fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): Sak? =
        data.get().values.find {
            it.behandlinger.any { behandling -> behandling.id == behandlingId }
        }

    override fun hentForSøknadId(søknadId: SøknadId): Sak? =
        data.get().values.find {
            it.behandlinger.any { behandling -> behandling.søknad.id == søknadId }
        }
}
