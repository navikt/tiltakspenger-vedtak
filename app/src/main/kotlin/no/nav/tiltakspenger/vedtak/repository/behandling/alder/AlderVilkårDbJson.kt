package no.nav.tiltakspenger.vedtak.repository.behandling.alder

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere AlderVilkår til og fra json for lagring i database.
 */
internal data class AlderVilkårDbJson(
    val søknadSaksopplysning: AlderSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: AlderSaksopplysningDbJson?,
    val avklartSaksopplysning: AlderSaksopplysningDbJson,
    val vurderingsperiode: PeriodeDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(): AlderVilkår {
        return AlderVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain(),
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            vurderingsperiode = vurderingsperiode.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun AlderVilkår.toDbJson(): AlderVilkårDbJson {
    return AlderVilkårDbJson(
        søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        vurderingsperiode = vurderingsperiode.toDbJson(),
        utfallsperioder = utfall.toDbJson(),
    )
}
