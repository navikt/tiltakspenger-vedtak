package no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.toDomain

/**
 * Har ansvar for å serialisere/deserialisere IntroVilkår til og fra json for lagring i database.
 */
internal data class IntroVilkårDbJson(
    val søknadSaksopplysning: IntroSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: IntroSaksopplysningDbJson?,
    val avklartSaksopplysning: IntroSaksopplysningDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(): IntroVilkår {
        return IntroVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain(),
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
    }
}

internal fun IntroVilkår.toDbJson(): IntroVilkårDbJson {
    return IntroVilkårDbJson(
        søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        utfallsperioder = utfall.toDbJson(),
    )
}
