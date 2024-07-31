package no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere TiltakDeltagelseVilkår til og fra json for lagring i database.
 */
internal data class TiltakDeltagelseVilkårDbJson(
    val registerSaksopplysning: TiltakDeltagelseSaksopplysningDbJson,
    val vurderingsperiode: PeriodeDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(): TiltakDeltagelseVilkår {
        return TiltakDeltagelseVilkår.fromDb(
            registerSaksopplysning = registerSaksopplysning.toDomain(),
            vurderingsperiode = vurderingsperiode.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun TiltakDeltagelseVilkår.toDbJson(): TiltakDeltagelseVilkårDbJson {
    return TiltakDeltagelseVilkårDbJson(
        registerSaksopplysning = registerSaksopplysning.toDbJson(),
        vurderingsperiode = vurderingsperiode.toDbJson(),
        utfallsperioder = utfall().toDbJson(),
    )
}
