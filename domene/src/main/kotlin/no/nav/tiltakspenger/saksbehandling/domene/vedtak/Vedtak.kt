package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsdetaljer
import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId = VedtakId.random(),
    val sakId: SakId,
    val behandling: BehandlingIverksatt,
    val vedtaksdato: LocalDateTime,
    val vedtaksType: VedtaksType,
    val periode: Periode,
    // val saksopplysninger: List<LivsoppholdSaksopplysning>,
    val utfallsperioder: Periodisering<Utfallsdetaljer>,
    // val vurderinger: List<Vurdering>,
    val saksbehandler: String,
    val beslutter: String,
)

enum class VedtaksType(val navn: String, val skalSendeBrev: Boolean) {
    AVSLAG("Avslag", true),
    INNVILGELSE("Innvilgelse", true),
    STANS("Stans", true),
    FORLENGELSE("Forlengelse", true),
}
