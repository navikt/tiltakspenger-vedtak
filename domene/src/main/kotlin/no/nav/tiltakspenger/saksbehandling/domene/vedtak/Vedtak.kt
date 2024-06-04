package no.nav.tiltakspenger.saksbehandling.domene.vedtak

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId = VedtakId.random(),
    val sakId: SakId,
    val behandling: Behandling,
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
