package no.nav.tiltakspenger.domene.vedtak

import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId = VedtakId.random(),
    val sakId: SakId,
    val behandling: BehandlingIverksatt,
    val vedtaksdato: LocalDateTime,
    val vedtaksType: VedtaksType,
    val periode: Periode,
    val saksopplysninger: List<Saksopplysning>,
    val utfallsperioder: List<Utfallsperiode>,
    val vurderinger: List<Vurdering>,
    val saksbehandler: String,
    val beslutter: String,
)

enum class VedtaksType(val navn: String, val skalSendeBrev: Boolean) {
    AVSLAG("Avslag", true),
    INNVILGELSE("Innvilgelse", true),
    STANS("Stans", true),
    FORLENGELSE("Forlengelse", true),
}
