package no.nav.tiltakspenger.saksbehandling.service.behandling

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.meldekort.domene.opprettFørsteMeldekortForEnSak
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attesteringsstatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.opprettVedtak
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.RammevedtakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.iverksettBehandlingMapper
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.stønadStatistikkMapper
import java.time.LocalDate

class BehandlingServiceImpl(
    private val førstegangsbehandlingRepo: BehandlingRepo,
    private val rammevedtakRepo: RammevedtakRepo,
    private val personopplysningRepo: PersonopplysningerRepo,
    private val meldekortRepo: MeldekortRepo,
    private val sakRepo: SakRepo,
    private val sessionFactory: SessionFactory,
    private val statistikkSakRepo: StatistikkSakRepo,
    private val statistikkStønadRepo: StatistikkStønadRepo,
) : BehandlingService {
    override fun hentBehandling(
        behandlingId: BehandlingId,
        sessionContext: SessionContext?,
    ): Behandling = førstegangsbehandlingRepo.hent(behandlingId, sessionContext)

    override fun hentBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        sessionContext: SessionContext?,
    ): Behandling {
        val behandling = hentBehandling(behandlingId, sessionContext)
        val sakPersonopplysninger = personopplysningRepo.hent(behandling.sakId)
        if (!sakPersonopplysninger.harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler har ikke tilgang til behandling")
        }
        return behandling
    }

    override fun hentBehandlingForSøknadId(søknadId: SøknadId): Førstegangsbehandling? {
        // TODO pre-mvp tilgang jah: Legg på sjekk på kode 6/7/skjermet.
        return førstegangsbehandlingRepo.hentForSøknadId(søknadId)
    }

    override fun sendTilBeslutter(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ) {
        val behandling = hentBehandling(behandlingId, utøvendeSaksbehandler).tilBeslutning(utøvendeSaksbehandler)
        førstegangsbehandlingRepo.lagre(behandling)
    }

    override fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        utøvendeBeslutter: Saksbehandler,
        begrunnelse: String,
    ) {
        val attestering =
            Attestering(
                status = Attesteringsstatus.SENDT_TILBAKE,
                begrunnelse = begrunnelse,
                beslutter = utøvendeBeslutter.navIdent,
            )
        val behandling = hentBehandling(behandlingId, utøvendeBeslutter).sendTilbake(utøvendeBeslutter, attestering)
        sessionFactory.withTransactionContext { tx ->
            førstegangsbehandlingRepo.lagre(behandling, tx)
        }
    }

    override suspend fun iverksett(
        behandlingId: BehandlingId,
        utøvendeBeslutter: Saksbehandler,
    ) {
        val behandling = hentBehandling(behandlingId, utøvendeBeslutter) as Førstegangsbehandling
        val sak =
            sakRepo.hentDetaljerForSakId(behandling.sakId)
                ?: throw IllegalStateException("iverksett finner ikke sak ${behandling.sakId}")
        val attestering =
            Attestering(
                status = Attesteringsstatus.GODKJENT,
                begrunnelse = "",
                beslutter = utøvendeBeslutter.navIdent,
            )
        val iverksattBehandling = behandling.iverksett(utøvendeBeslutter, attestering)

        val vedtak = iverksattBehandling.opprettVedtak()
        val sakStatistikk = iverksettBehandlingMapper(sak, iverksattBehandling, vedtak)
        val stønadStatistikk = stønadStatistikkMapper(sak, vedtak)
        val førsteMeldekort = vedtak.opprettFørsteMeldekortForEnSak()
        sessionFactory.withTransactionContext { tx ->
            førstegangsbehandlingRepo.lagre(iverksattBehandling, tx)
            rammevedtakRepo.lagre(vedtak, tx)
            statistikkSakRepo.lagre(sakStatistikk, tx)
            statistikkStønadRepo.lagre(stønadStatistikk, tx)
            meldekortRepo.lagre(førsteMeldekort, tx)
        }
        // journalføring og dokumentdistribusjon skjer i egen jobb
    }

    override fun taBehandling(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ): Behandling {
        val behandling = hentBehandling(behandlingId, utøvendeSaksbehandler)
        return behandling.taBehandling(utøvendeSaksbehandler).also {
            førstegangsbehandlingRepo.lagre(it)
        }
    }

    override fun frataBehandling(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ) {
        val behandling = hentBehandling(behandlingId, utøvendeSaksbehandler)
        førstegangsbehandlingRepo.lagre(behandling.taSaksbehandlerAvBehandlingen(utøvendeSaksbehandler))
    }

    // er tenkt brukt fra datadeling og henter alle behandlinger som ikke er iverksatt for en ident
    override fun hentBehandlingerUnderBehandlingForIdent(
        ident: Fnr,
        fom: LocalDate,
        tom: LocalDate,
    ): List<Behandling> =
        førstegangsbehandlingRepo
            .hentAlleForIdent(ident)
            .filter { behandling -> !behandling.erIverksatt() }
            .filter { behandling ->
                behandling.vurderingsperiode.overlapperMed(
                    Periode(
                        fraOgMed = fom,
                        tilOgMed = tom,
                    ),
                )
            }
}
