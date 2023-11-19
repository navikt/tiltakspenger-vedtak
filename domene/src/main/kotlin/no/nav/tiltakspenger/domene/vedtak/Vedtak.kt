package no.nav.tiltakspenger.domene.vedtak

import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.VedtakId
import java.time.LocalDate

data class Vedtak(
    val id: VedtakId = VedtakId.random(),
    val behandling: BehandlingIverksatt,
    val vedtaksdato: LocalDate,
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
