package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall

data class SaksopplysningOgUtfallForPeriode(
    val vilkår: Inngangsvilkår,
    val kilde: Kilde,
    val detaljer: String,
    val saksbehandler: String?,
    val harYtelse: HarYtelse,
    val utfall: Utfall,
)

data class LivsoppholdSaksopplysningOgUtfallForPeriode(
    val vilkår: LivsoppholdDelVilkår,
    val kilde: Kilde,
    val detaljer: String,
    val saksbehandler: String?,
    val harYtelse: HarYtelse,
    val utfall: Utfall,
)
