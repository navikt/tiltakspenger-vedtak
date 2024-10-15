package no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain

/**
 * Har ansvar for å serialisere/deserialisere IntroVilkår til og fra json for lagring i database.
 */
internal data class IntroVilkårDbJson(
    val søknadSaksopplysning: IntroSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: IntroSaksopplysningDbJson?,
    val avklartSaksopplysning: IntroSaksopplysningDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(vurderingsperiode: Periode): IntroVilkår =
        IntroVilkår.fromDb(
            vurderingsperiode = vurderingsperiode,
            søknadSaksopplysning = søknadSaksopplysning.toDomain() as IntroSaksopplysning.Søknad,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain() as IntroSaksopplysning.Saksbehandler?,
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
}

internal fun IntroVilkår.toDbJson(): IntroVilkårDbJson =
    IntroVilkårDbJson(
        søknadSaksopplysning = søknadSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        utfallsperioder = utfall.toDbJson(),
    )
