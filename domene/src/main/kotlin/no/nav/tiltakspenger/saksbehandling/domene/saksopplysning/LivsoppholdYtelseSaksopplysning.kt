package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår

data class LivsoppholdYtelseSaksopplysning(
    val kilde: Kilde,
    val vilkår: LivsoppholdDelVilkår,
    val detaljer: String,
    val harYtelse: Periodisering<HarYtelse>,
    val saksbehandler: String? = null,
)
