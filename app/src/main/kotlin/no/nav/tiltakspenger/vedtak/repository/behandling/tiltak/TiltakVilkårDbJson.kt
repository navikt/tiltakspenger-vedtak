package no.nav.tiltakspenger.vedtak.repository.behandling.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.TiltakVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere TiltakVilkår til og fra json for lagring i database.
 */
internal data class TiltakVilkårDbJson(
    val søknadSaksopplysning: TiltakSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: TiltakSaksopplysningDbJson?,
    val avklartSaksopplysning: TiltakSaksopplysningDbJson,
    val vurderingsperiode: PeriodeDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(): TiltakVilkår {
        return TiltakVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain(),
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            vurderingsperiode = vurderingsperiode.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun TiltakVilkår.toDbJson(): TiltakVilkårDbJson {
    return TiltakVilkårDbJson(
        søknadSaksopplysning = registerSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        vurderingsperiode = vurderingsperiode.toDbJson(),
        utfallsperioder = utfall.toDbJson(),
    )
}
