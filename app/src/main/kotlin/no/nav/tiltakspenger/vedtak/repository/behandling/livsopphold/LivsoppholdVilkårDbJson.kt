package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere KvpVilkår til og fra json for lagring i database.
 */
internal data class LivsoppholdVilkårDbJson(
    val søknadSaksopplysning: LivsoppholdSaksopplysningDbJson,
    val saksbehandlerSaksopplysning: LivsoppholdSaksopplysningDbJson?,
    val avklartSaksopplysning: LivsoppholdSaksopplysningDbJson?,
    val vurderingsPeriode: PeriodeDbJson,
) {
    fun toDomain(): LivsoppholdVilkår {
        return LivsoppholdVilkår.fromDb(
            søknadSaksopplysning = søknadSaksopplysning.toDomain(),
            saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDomain(),
            avklartSaksopplysning = avklartSaksopplysning?.toDomain(),
            vurderingsPeriode = vurderingsPeriode.toDomain(),
        )
    }
}

internal fun LivsoppholdVilkår.toDbJson(): LivsoppholdVilkårDbJson {
    return LivsoppholdVilkårDbJson(
        søknadSaksopplysning = søknadssaksopplysning.toDbJson(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        avklartSaksopplysning = avklartSaksopplysning?.toDbJson(),
        vurderingsPeriode = vurderingsPeriode.toDbJson(),
    )
}
