package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class LivoppholdSaksopplysning(
    val kilde: Kilde,
    val vilkår: Vilkår,
    val detaljer: String,
    val harYtelse: Periodisering<HarYtelse?>,
    val saksbehandler: String? = null,
)
