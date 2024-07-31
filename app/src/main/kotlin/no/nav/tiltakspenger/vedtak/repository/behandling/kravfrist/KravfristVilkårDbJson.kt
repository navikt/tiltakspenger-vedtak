package no.nav.tiltakspenger.vedtak.repository.behandling.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere KravfristVilkår til og fra json for lagring i database.
 */
internal data class KravfristVilkårDbJson(
    val søknadSaksopplysning: KravfristSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: KravfristSaksopplysningDbJson?,
    val avklartSaksopplysning: KravfristSaksopplysningDbJson,
    val vurderingsperiode: PeriodeDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(): KravfristVilkår {
        return KravfristVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain(),
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            vurderingsperiode = vurderingsperiode.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun KravfristVilkår.toDbJson(): KravfristVilkårDbJson {
    return KravfristVilkårDbJson(
        søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        vurderingsperiode = vurderingsperiode.toDbJson(),
        utfallsperioder = utfall().toDbJson(),
    )
}
