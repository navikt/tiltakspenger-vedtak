package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.distribusjon.domene.DistribusjonId
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.meldekort.domene.sisteGodkjenteMeldekort
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingstype
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @param opprettet Tidspunktet vi instansierte og persisterte dette utbetalingsvedtaket første gangen. Dette har ingenting med vedtaksbrevet å gjøre.
 */
data class Rammevedtak(
    override val id: VedtakId = VedtakId.random(),
    override val opprettet: LocalDateTime,
    val sakId: SakId,
    val behandling: Behandling,
    val vedtaksdato: LocalDate?,
    val vedtaksType: Vedtakstype,
    val periode: Periode,
    val journalpostId: JournalpostId?,
    val journalføringstidspunkt: LocalDateTime?,
    val distribusjonId: DistribusjonId?,
    val distribusjonstidspunkt: LocalDateTime?,
    val sendtTilDatadeling: LocalDateTime?,
    val brevJson: String?,
) : Vedtak {
    val fnr: Fnr = behandling.fnr
    val saksnummer: Saksnummer = behandling.saksnummer
    val saksbehandlerNavIdent: String = behandling.saksbehandler!!
    val beslutterNavIdent: String = behandling.beslutter!!
    val utfallsperioder: Periodisering<AvklartUtfallForPeriode> get() = behandling.avklarteUtfallsperioder
    override val antallDagerPerMeldeperiode: Int = behandling.maksDagerMedTiltakspengerForPeriode

    override fun erStansvedtak(): Boolean {
        return vedtaksType == Vedtakstype.STANS
    }

    init {
        require(behandling.erIverksatt) { "Kan ikke lage vedtak for behandling som ikke er iverksatt. BehandlingId: ${behandling.id}" }
        require(sakId == behandling.sakId) { "SakId i vedtak og behandling må være lik. SakId: $sakId, BehandlingId: ${behandling.id}" }
        require(periode == behandling.vurderingsperiode) { "Periode i vedtak og behandling må være lik. Periode: $periode, BehandlingId: ${behandling.id}" }
    }
}

enum class Vedtakstype(
    val navn: String,
) {
    INNVILGELSE("Innvilgelse"),
    STANS("Stans"),
}

fun Sak.opprettVedtak(
    behandling: Behandling,
): Rammevedtak {
    require(behandling.status == Behandlingsstatus.INNVILGET) { "Kan ikke lage vedtak for behandling som ikke er iverksatt" }
    return Rammevedtak(
        id = VedtakId.random(),
        opprettet = nå(),
        sakId = this.id,
        behandling = behandling,
        vedtaksdato = null,
        vedtaksType = this.utledVedtakstype(behandling),
        periode = behandling.vurderingsperiode,
        journalpostId = null,
        journalføringstidspunkt = null,
        distribusjonId = null,
        distribusjonstidspunkt = null,
        sendtTilDatadeling = null,
        brevJson = null,
    )
}

fun Sak.utledVedtakstype(behandling: Behandling): Vedtakstype {
    return when (behandling.behandlingstype) {
        Behandlingstype.FØRSTEGANGSBEHANDLING -> Vedtakstype.INNVILGELSE
        Behandlingstype.REVURDERING -> {
            // Kommentar jah: Dette er en førsteimplementasjon for å avgjøre om dette er et stansvedtak. Ved andre typer revurderinger må vi utvide denne.
            if (behandling.vurderingsperiode.tilOgMed != this.utfallsperioder()!!.totalePeriode.tilOgMed) {
                throw IllegalStateException("Kan ikke lage stansvedtak for revurdering - revurderingens tilOgMed må være lik sakens tilOgMed")
            }
            if (!behandling.erHelePeriodenIkkeOppfylt) {
                throw IllegalStateException("Kan ikke lage stansvedtak for revurdering - hele perioden må være 'ikke oppfylt'")
            }
            val sisteGodkjenteMeldekort = this.sisteGodkjenteMeldekort() ?: return Vedtakstype.STANS
            if (sisteGodkjenteMeldekort.tilOgMed.isBefore(behandling.vurderingsperiode.fraOgMed)) {
                Vedtakstype.STANS
            } else {
                throw IllegalStateException("Kan ikke lage stansvedtak for revurdering - godkjent meldekort overlapper revurderingsperioden")
            }
        }
    }
}
