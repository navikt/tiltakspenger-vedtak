package no.nav.tiltakspenger.vedtak.repository.behandling.kravdato

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato.KravdatoVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere KravdatoVilkår til og fra json for lagring i database.
 */
internal data class KravdatoVilkårDbJson(
    val søknadSaksopplysning: KravdatoSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: KravdatoSaksopplysningDbJson?,
    val avklartSaksopplysning: KravdatoSaksopplysningDbJson,
    val vurderingsperiode: PeriodeDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(): KravdatoVilkår {
        return KravdatoVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain(),
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            vurderingsperiode = vurderingsperiode.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun KravdatoVilkår.toDbJson(): KravdatoVilkårDbJson {
    return KravdatoVilkårDbJson(
        søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        vurderingsperiode = vurderingsperiode.toDbJson(),
        utfallsperioder = utfall.toDbJson(),
    )
}
