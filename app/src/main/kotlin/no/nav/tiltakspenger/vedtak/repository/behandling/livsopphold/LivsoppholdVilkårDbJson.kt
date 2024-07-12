package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

/**
 * Har ansvar for å serialisere/deserialisere KvpVilkår til og fra json for lagring i database.
 */
internal data class LivsoppholdVilkårDbJson(
    val harEnEllerFlereYtelserFraSøknaden: LivsoppholdSaksopplysningDbJson,
    val livsoppholdSaksopplysning: LivsoppholdSaksopplysningDbJson?,
    val vurderingsPeriode: PeriodeDbJson,
) {
    fun toDomain(): LivsoppholdVilkår {
        return LivsoppholdVilkår.fromDb(
            harEnEllerFlereYtelserFraSøknaden = harEnEllerFlereYtelserFraSøknaden.toDomain(),
            livsoppholdSaksopplysning = livsoppholdSaksopplysning?.toDomain(),
            vurderingsPeriode = vurderingsPeriode.toDomain(),
        )
    }
}

internal fun LivsoppholdVilkår.toDbJson(): LivsoppholdVilkårDbJson {
    return LivsoppholdVilkårDbJson(
        harEnEllerFlereYtelserFraSøknaden = søknadssaksopplysning.toDbJson(),
        livsoppholdSaksopplysning = saksbehandlerSaksopplysning?.toDbJson(),
        vurderingsPeriode = vurderingsPeriode.toDbJson(),
    )
}
