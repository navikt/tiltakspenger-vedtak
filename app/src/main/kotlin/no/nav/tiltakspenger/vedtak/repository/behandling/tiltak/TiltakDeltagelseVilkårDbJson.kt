package no.nav.tiltakspenger.vedtak.repository.behandling.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere TiltakVilkår til og fra json for lagring i database.
 */
internal data class TiltakDeltagelseVilkårDbJson(
    val søknadSaksopplysning: TiltakSaksopplysningDbJson,
    val vurderingsperiode: PeriodeDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(): TiltakDeltagelseVilkår {
        return TiltakDeltagelseVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain(),
            vurderingsperiode = vurderingsperiode.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun TiltakDeltagelseVilkår.toDbJson(): TiltakDeltagelseVilkårDbJson {
    return TiltakDeltagelseVilkårDbJson(
        søknadSaksopplysning = registerSaksopplysning.toDbJson(),
        vurderingsperiode = vurderingsperiode.toDbJson(),
        utfallsperioder = utfall.toDbJson(),
    )
}
