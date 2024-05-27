package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class JaNeiPeriodeVilkår private constructor(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val opprinneligSaksopplysning: JaNeiPeriodeSaksopplysning,
    val korrigertSaksopplysning: JaNeiPeriodeSaksopplysning?,
    val avklartSaksopplysning: JaNeiPeriodeSaksopplysning,
    val vurdering: Periodisering<Utfall>,
)
