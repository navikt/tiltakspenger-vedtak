package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.distribusjon.domene.DistribusjonId
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.LocalDateTime

data class Rammevedtak(
    override val id: VedtakId = VedtakId.random(),
    val sakId: SakId,
    val saksnummer: Saksnummer,
    val behandling: Førstegangsbehandling,
    val vedtaksdato: LocalDateTime,
    val vedtaksType: Vedtakstype,
    val periode: Periode,
    val saksbehandlerNavIdent: String,
    val beslutterNavIdent: String,
    val journalpostId: JournalpostId?,
    val journalføringstidstpunkt: LocalDateTime?,
    val distribusjonId: DistribusjonId?,
    val distribusjonstidspunkt: LocalDateTime?,
    val sendtTilDatadeling: LocalDateTime?,
) : Vedtak {
    val fnr = behandling.fnr
    val utfallsperioder: Periodisering<AvklartUtfallForPeriode> get() = behandling.avklarteUtfallsperioder
    override val antallDagerPerMeldeperiode: Int = behandling.antallDagerPerMeldeperiode
}

enum class Vedtakstype(
    val navn: String,
) {
    AVSLAG("Avslag"),
    INNVILGELSE("Innvilgelse"),
    STANS("Stans"),
    FORLENGELSE("Forlengelse"),
}

fun Førstegangsbehandling.opprettVedtak(): Rammevedtak {
    require(this.status == Behandlingsstatus.INNVILGET) { "Kan ikke lage vedtak for behandling som ikke er iverksatt" }
    return Rammevedtak(
        id = VedtakId.random(),
        sakId = this.sakId,
        saksnummer = this.saksnummer,
        behandling = this,
        vedtaksdato = nå(),
        vedtaksType = Vedtakstype.INNVILGELSE,
        periode = this.vurderingsperiode,
        saksbehandlerNavIdent = this.saksbehandler!!,
        beslutterNavIdent = this.beslutter!!,
        journalpostId = null,
        journalføringstidstpunkt = null,
        distribusjonId = null,
        distribusjonstidspunkt = null,
        sendtTilDatadeling = null,
    )
}
