package no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain

/**
 * Har ansvar for å serialisere/deserialisere TiltakDeltagelseVilkår til og fra json for lagring i database.
 */
internal data class TiltakDeltagelseVilkårDbJson(
    val registerSaksopplysning: TiltakDeltagelseSaksopplysningDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(vurderingsperiode: Periode): TiltakDeltagelseVilkår =
        TiltakDeltagelseVilkår.fromDb(
            registerSaksopplysning = registerSaksopplysning.toDomain() as TiltakDeltagelseSaksopplysning.Register,
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
        )
}

internal fun TiltakDeltagelseVilkår.toDbJson(): TiltakDeltagelseVilkårDbJson =
    TiltakDeltagelseVilkårDbJson(
        registerSaksopplysning = registerSaksopplysning.toDbJson(),
        utfallsperioder = utfall.toDbJson(),
    )
