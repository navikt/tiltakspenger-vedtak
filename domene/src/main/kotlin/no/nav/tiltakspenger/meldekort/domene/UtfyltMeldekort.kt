package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode

/**
 * Meldekort utfylt av saksbehandler og godkjent av beslutter.
 * Når veileder/bruker har fylt ut meldekortet vil ikke denne klassen kunne gjenbrukes uten endringer. Kanskje vi må ha en egen klasse for veileder-/brukerutfylt meldekort.
 *
 * @param saksbehandler: Obligatorisk dersom meldekortet er utfylt av saksbehandler.
 * @param beslutter: Obligatorisk dersom meldekortet er godkjent av beslutter.
 */
data class UtfyltMeldekort(
    val id: MeldekortId,
    val sakId: SakId,
    val rammevedtakId: VedtakId,
    // TODO jah: Tanken er at vi heller tar i mot brukers deltagelses-svar og gjør beregninga her. Men utsetter det til neste PR, da det er et større arbeid.
    val meldekortperiode: Meldekortperiode,
    val saksbehandler: String,
    val beslutter: String,
) {
    val periode: Periode = meldekortperiode.periode
}
