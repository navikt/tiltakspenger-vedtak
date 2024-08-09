package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.AvklartUtfallForPeriode
import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId = VedtakId.random(),
    val sakId: SakId,
    val behandling: Behandling,
    val vedtaksdato: LocalDateTime,
    val vedtaksType: VedtaksType,
    val periode: Periode,
    val saksbehandler: String,
    val beslutter: String,
) {
    val utfallsperioder: Periodisering<AvklartUtfallForPeriode> get() = behandling.avklarteUtfallsperioder
}

enum class VedtaksType(
    val navn: String,
    val skalSendeBrev: Boolean,
) {
    AVSLAG("Avslag", true),
    INNVILGELSE("Innvilgelse", true),
    STANS("Stans", true),
    FORLENGELSE("Forlengelse", true),
}

fun Førstegangsbehandling.opprettVedtak(): Vedtak {
    require(this.status == Behandlingsstatus.INNVILGET) { "Kan ikke lage vedtak for behandling som ikke er iverksatt" }
    return Vedtak(
        id = VedtakId.random(),
        sakId = this.sakId,
        behandling = this,
        vedtaksdato = LocalDateTime.now(),
        vedtaksType = VedtaksType.INNVILGELSE,
        periode = this.vurderingsperiode,
        saksbehandler = this.saksbehandler!!,
        beslutter = this.beslutter!!,
    )
}
