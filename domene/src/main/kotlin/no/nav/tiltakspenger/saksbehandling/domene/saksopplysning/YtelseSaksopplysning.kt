package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår

data class YtelseSaksopplysning(
    val kilde: Kilde,
    val vilkår: Inngangsvilkår,
    val detaljer: String,
    val harYtelse: Periodisering<HarYtelse>,
    val saksbehandler: String? = null,
)
