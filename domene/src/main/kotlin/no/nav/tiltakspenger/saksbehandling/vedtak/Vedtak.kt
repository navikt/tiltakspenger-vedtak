package no.nav.tiltakspenger.saksbehandling.vedtak

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.saksbehandling.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.vilkår.Vurdering
import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId = VedtakId.random(),
    val sakId: SakId,
    val behandling: BehandlingIverksatt,
    val vedtaksdato: LocalDateTime,
    val vedtaksType: VedtaksType,
    // de under kan hentes fra behandling men kanskje kjekt å ha her også?
    val periode: Periode,
    val saksopplysninger: List<Saksopplysning>,
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
