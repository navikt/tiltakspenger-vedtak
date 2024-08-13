package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.PeriodisertUtfallDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.toDomain

/**
 * Har ansvar for å serialisere/deserialisere LivsoppholdVilkår til og fra json for lagring i database.
 */
internal data class LivsoppholdVilkårDbJson(
    val søknadSaksopplysning: LivsoppholdSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysningDbJson?,
    val avklartSaksopplysning: LivsoppholdSaksopplysningDbJson?,
    val utfallsperioder: List<PeriodisertUtfallDbJson>,
) {
    fun toDomain(vurderingsperiode: Periode): LivsoppholdVilkår =
        LivsoppholdVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain() as LivsoppholdSaksopplysning.Søknad,
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain() as LivsoppholdSaksopplysning.Saksbehandler?,
            avklartSaksopplysning = avklartSaksopplysning?.toDomain(),
            vurderingsperiode = vurderingsperiode,
            utfall = utfallsperioder.toDomain(),
        )
}

internal fun LivsoppholdVilkår.toDbJson(): LivsoppholdVilkårDbJson =
    LivsoppholdVilkårDbJson(
        søknadSaksopplysning = søknadssaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning?.toDbJson(),
        utfallsperioder = utfall().toDbJson(),
    )
