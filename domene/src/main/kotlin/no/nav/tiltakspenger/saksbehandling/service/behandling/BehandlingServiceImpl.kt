package no.nav.tiltakspenger.saksbehandling.service.behandling

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.RevurderingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Revurderingsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import no.nav.tiltakspenger.saksbehandling.ports.MultiRepo
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
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
    private val multiRepo: MultiRepo,
    private val sakRepo: SakRepo,
) : BehandlingService {

    override fun hentBehandlingOrNull(behandlingId: BehandlingId): Behandling? {
        return behandlingRepo.hentOrNull(behandlingId)
    }

    override fun hentBehandling(behandlingId: BehandlingId): Behandling {
        return behandlingRepo.hentOrNull(behandlingId)
            ?: throw IkkeFunnetException("Behandling med id $behandlingId ikke funnet")
    }

    override fun hentBehandlingForJournalpostId(journalpostId: String): Førstegangsbehandling? {
        return behandlingRepo.hentForJournalpostId(journalpostId)
    }

    override fun hentAlleBehandlinger(saksbehandler: Saksbehandler): List<Førstegangsbehandling> {
        return behandlingRepo.hentAlle()
            .filter { behandling -> personopplysningRepo.hent(behandling.sakId).harTilgang(saksbehandler) }
    }

    override fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning) {
        val behandlingRespons = hentBehandling(behandlingId)
            .leggTilSaksopplysning(emptyList()) // TODO: Her har det skjedde en quickfix for å gjøre kompilatoren glad 🙈
        if (behandlingRespons.erEndret) behandlingRepo.lagre(behandlingRespons.behandling)
    }

    override fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>) {
        val behandling = hentBehandling(behandlingId)
        val oppdatertBehandling = behandling.oppdaterTiltak(
            tiltak.filter {
                Periode(it.deltakelseFom, it.deltakelseTom).overlapperMed(behandling.vurderingsperiode)
            },
        )
        behandlingRepo.lagre(oppdatertBehandling)
    }

    override fun sendTilBeslutter(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ) {
        check(utøvendeSaksbehandler.roller.contains(Rolle.SAKSBEHANDLER)) { "Saksbehandler må være saksbehandler" }
        val behandling = hentBehandling(behandlingId)
        if (behandling is BehandlingVilkårsvurdert) {
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

        when (behandling) {
            is BehandlingTilBeslutter -> {
                multiRepo.lagre(behandling.sendTilbake(utøvendeBeslutter), attestering)
            }

            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke sendes tilbake til saksbehandler. BehandlingId: $behandlingId")
        }
    }

    override suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler) {
        val behandling = hentBehandling(behandlingId)
        val sak = sakRepo.hentSakDetaljer(behandling.sakId)
            ?: throw IllegalStateException("iverksett finner ikke sak ${behandling.sakId}")

        val iverksattBehandling = when (behandling) {
            is BehandlingTilBeslutter -> behandling.iverksett(utøvendeBeslutter)
            else -> throw IllegalStateException("Behandlingen har feil tilstand og kan ikke iverksettes. BehandlingId: $behandlingId")
        }
        val attestering = Attestering(
            behandlingId = behandlingId,
            svar = AttesteringStatus.GODKJENT,
            begrunnelse = null,
            beslutter = utøvendeBeslutter.navIdent,
        )

        val vedtak = lagVedtakForBehandling(iverksattBehandling)
        multiRepo.lagreOgKjør(iverksattBehandling, attestering, vedtak) {
            // Hvis kallet til utbetalingService feiler, kastes det en exception slik at vi ikke lagrer vedtaket og
            // sender melding til brev og meldekortgrunnlag. Dette er med vilje.
            utbetalingService.sendBehandlingTilUtbetaling(sak, vedtak)
        }

        meldekortGrunnlagGateway.sendMeldekortGrunnlag(sak, vedtak)

        val personopplysninger =
            personopplysningRepo.hent(vedtak.sakId).søker()
        brevPublisherGateway.sendBrev(vedtak, personopplysninger)
    }

    private fun lagVedtakForBehandling(behandling: BehandlingIverksatt): Vedtak {
        return Vedtak(
            id = VedtakId.random(),
            sakId = behandling.sakId,
            behandling = behandling,
            vedtaksdato = LocalDateTime.now(),
            vedtaksType = if (behandling.status == BehandlingStatus.Innvilget) VedtaksType.INNVILGELSE else VedtaksType.AVSLAG,
            utfallsperioder = behandling.utfallsperioder,
            periode = behandling.vurderingsperiode,
            saksopplysninger = emptyList(), // TODO: Her har det skjedd en quickfix for å gjøre kompilatoren glad 🙈
            vurderinger = emptyList(), // TODO: Her har det skjedd en quickfix for å gjøre kompilatoren glad 🙈
            saksbehandler = behandling.saksbehandler,
            beslutter = behandling.beslutter,
        )
    }

    // TODO: Burde det vært to ulike funksjoner avhengig av om det er saksbehandler eller beslutter det gjelder?
    override fun taBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler) {
        val behandling = hentBehandling(behandlingId)
        behandlingRepo.lagre(behandling.startBehandling(utøvendeSaksbehandler))
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

    override fun opprettRevurdering(
        behandlingId: BehandlingId,
        utøvendeSaksbehandler: Saksbehandler,
    ): Revurderingsbehandling {
        check(utøvendeSaksbehandler.roller.contains(Rolle.SAKSBEHANDLER)) { "Saksbehandler må være saksbehandler" }

        val vedtak = vedtakRepo.hentVedtakForBehandling(behandlingId)
        val revurderingBehandling = RevurderingOpprettet.opprettRevurderingsbehandling(
            vedtak = vedtak,
            navIdent = utøvendeSaksbehandler.navIdent,
        )

        behandlingRepo.lagre(revurderingBehandling)

        return revurderingBehandling
    }
}
