package no.nav.tiltakspenger.saksbehandling.service.behandling

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingerForBenk
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.saksbehandling.ports.VedtakRepo
import no.nav.tiltakspenger.saksbehandling.service.utbetaling.UtbetalingService
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class BehandlingServiceImpl(
    private val behandlingRepo: BehandlingRepo,
    private val vedtakRepo: VedtakRepo,
    private val personopplysningRepo: PersonopplysningerRepo,
    private val utbetalingService: UtbetalingService,
    private val brevPublisherGateway: BrevPublisherGateway,
    private val meldekortGrunnlagGateway: MeldekortGrunnlagGateway,
    private val sakRepo: SakRepo,
    private val attesteringRepo: AttesteringRepo,
    private val sessionFactory: SessionFactory,
    private val søknadRepo: SøknadRepo,
) : BehandlingService {

    override fun hentBehandling(behandlingId: BehandlingId, sessionContext: SessionContext?): Behandling {
        return behandlingRepo.hent(behandlingId, sessionContext)
    }

    override fun hentBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        sessionContext: SessionContext?,
    ): Behandling {
        val behandling = hentBehandling(behandlingId, sessionContext)
        if (!personopplysningRepo.hent(behandling.sakId).harTilgang(saksbehandler)) {
            throw TilgangException("Saksbehandler har ikke tilgang til behandling")
        }
        return behandling
    }

    override fun hentBehandlingForSøknadId(søknadId: SøknadId): Førstegangsbehandling? {
        return behandlingRepo.hentForSøknadId(søknadId)
    }

    override fun hentBehandlingForJournalpostId(journalpostId: String): Førstegangsbehandling? {
        return behandlingRepo.hentForJournalpostId(journalpostId)
    }

    override fun hentBehandlingerForBenk(saksbehandler: Saksbehandler): BehandlingerForBenk {
        require(saksbehandler.isSaksbehandler())
        return BehandlingerForBenk(
            behandlinger = behandlingRepo.hentAlle()
                .filter { behandling -> personopplysningRepo.hent(behandling.sakId).harTilgang(saksbehandler) },
            søknader = søknadRepo.hentAlleSøknader(), // TODO jah: Her må vi gjøre et bulkkall til pdl sin egen rettigheter funksjon for å sjekke tilgang.
        )
    }

    override fun sendTilBeslutter(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ) {
        require(utøvendeSaksbehandler.isSaksbehandler()) { "Saksbehandler må har rollen SAKSBEHANDLEr" }
        val behandling = hentBehandling(behandlingId)
        if (behandling.tilstand == BehandlingTilstand.UNDER_BEHANDLING) {
            behandlingRepo.lagre(behandling.tilBeslutting(utøvendeSaksbehandler))
        }
    }

    override fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        utøvendeBeslutter: Saksbehandler,
        begrunnelse: String?,
    ) {
        val behandling = hentBehandling(behandlingId)

        checkNotNull(begrunnelse) { "Begrunnelse må oppgis når behandling sendes tilbake til saksbehandler" }
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.SENDT_TILBAKE,
            begrunnelse = begrunnelse,
            beslutter = utøvendeBeslutter.navIdent,
        )

        when (behandling.tilstand) {
            BehandlingTilstand.TIL_BESLUTTER -> {
                sessionFactory.withTransactionContext { tx ->
                    behandlingRepo.lagre(behandling.sendTilbake(utøvendeBeslutter), tx)
                    attesteringRepo.lagre(attestering, tx)
                }
            }

            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke sendes tilbake til saksbehandler. BehandlingId: $behandlingId")
        }
    }

    override suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler) {
        val behandling = hentBehandling(behandlingId)
        val sak = sakRepo.hentSakDetaljer(behandling.sakId)
            ?: throw IllegalStateException("iverksett finner ikke sak ${behandling.sakId}")

        val iverksattBehandling = when (behandling.tilstand) {
            BehandlingTilstand.TIL_BESLUTTER -> behandling.iverksett(utøvendeBeslutter)
            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke iverksettes. BehandlingId: $behandlingId")
        }
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.GODKJENT,
            begrunnelse = null,
            beslutter = utøvendeBeslutter.navIdent,
        )

        val vedtak = lagVedtakForBehandling(iverksattBehandling)
        sessionFactory.withTransactionContext { tx ->
            behandlingRepo.lagre(iverksattBehandling, tx)
            attesteringRepo.lagre(attestering, tx)
            vedtakRepo.lagreVedtak(vedtak, tx)

            runBlocking { utbetalingService.sendBehandlingTilUtbetaling(sak, vedtak) }
        }

        meldekortGrunnlagGateway.sendMeldekortGrunnlag(sak, vedtak)

        val personopplysninger = personopplysningRepo.hent(vedtak.sakId).søker()
        brevPublisherGateway.sendBrev(sak.saksnummer, vedtak, personopplysninger)
    }

    private fun lagVedtakForBehandling(behandling: Behandling): Vedtak {
        require(behandling.tilstand == BehandlingTilstand.IVERKSATT) { "Kan ikke lage vedtakk for behandling som ikke er iverksatt" }
        require(behandling.saksbehandler != null) { "Kan ikke lage vedtakk for behandling som ikke har saksbehandler" }
        require(behandling.beslutter != null) { "Kan ikke lage vedtakk for behandling som ikke har beslutter" }
        return Vedtak(
            id = VedtakId.random(),
            sakId = behandling.sakId,
            behandling = behandling,
            vedtaksdato = LocalDateTime.now(),
            vedtaksType = if (behandling.status == BehandlingStatus.Innvilget) VedtaksType.INNVILGELSE else VedtaksType.AVSLAG,
            utfallsperioder = behandling.vilkårssett.utfallsperioder(),
            periode = behandling.vurderingsperiode,
            saksbehandler = behandling.saksbehandler!!,
            beslutter = behandling.beslutter!!,
        )
    }

    override fun taBehandling(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ): Behandling {
        return sessionFactory.withTransactionContext { tx ->
            val behandling = hentBehandling(behandlingId, tx)
            taBehandling(behandling, utøvendeSaksbehandler, tx)
        }
    }

    override fun taBehandling(
        behandling: Behandling,
        utøvendeSaksbehandler: Saksbehandler,
        transactionContext: TransactionContext,
    ): Behandling {
        return sessionFactory.withTransactionContext(transactionContext) { tx ->
            behandling.taBehandling(utøvendeSaksbehandler).also {
                behandlingRepo.lagre(it, tx)
            }
        }
    }

    override fun frataBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler) {
        val behandling = hentBehandling(behandlingId)
        behandlingRepo.lagre(behandling.avbrytBehandling(utøvendeSaksbehandler))
    }

    override fun hentBehandlingForIdent(
        ident: String,
        utøvendeSaksbehandler: Saksbehandler,
    ): List<Førstegangsbehandling> {
        return behandlingRepo.hentAlleForIdent(ident)
            .filter { behandling -> personopplysningRepo.hent(behandling.sakId).harTilgang(utøvendeSaksbehandler) }
    }
}
