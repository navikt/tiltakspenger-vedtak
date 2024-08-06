package no.nav.tiltakspenger.vedtak.repository.behandling.alder

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain

/**
 * Har ansvar for å serialisere/deserialisere AlderVilkår til og fra json for lagring i database.
 */
internal data class AlderVilkårDbJson(
    val registerSaksopplysning: AlderSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: AlderSaksopplysningDbJson?,
    val avklartSaksopplysning: AlderSaksopplysningDbJson,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(vurderingsperiode: Periode): AlderVilkår =
        AlderVilkår.fromDb(
            vurderingsperiode = vurderingsperiode,
            registerSaksopplysning = registerSaksopplysning.toDomain() as AlderSaksopplysning.Register,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain() as AlderSaksopplysning.Saksbehandler?,
            avklartSaksopplysning = avklartSaksopplysning.toDomain(),
            utfall = utfallsperioder.toDomain(),
        )
}

internal fun AlderVilkår.toDbJson(): AlderVilkårDbJson =
    AlderVilkårDbJson(
        registerSaksopplysning = registerSaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning.toDbJson(),
        utfallsperioder = utfall().toDbJson(),
    )
