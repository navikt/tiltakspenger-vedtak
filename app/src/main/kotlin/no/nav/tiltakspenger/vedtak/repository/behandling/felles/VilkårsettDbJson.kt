package no.nav.tiltakspenger.vedtak.repository.behandling.felles

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.vedtak.db.deserialize
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.behandling.alder.AlderVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.alder.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.institusjonsopphold.InstitusjonsoppholdVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.institusjonsopphold.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet.IntroVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kravfrist.KravfristVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kravfrist.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.KVPVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.LivsoppholdVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse.TiltakDeltagelseVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse.toDbJson
import java.security.InvalidParameterException

/**
 * Har ansvar for å serialisere/deserialisere Vilkårssett til og fra json for lagring i database.
 */
private class VilkårssettJson(
    val institusjonsoppholdVilkår: InstitusjonsoppholdVilkårDbJson,
    val kvpVilkår: KVPVilkårDbJson,
    val introVilkår: IntroVilkårDbJson,
    val livsoppholdVilkår: LivsoppholdVilkårDbJson,
    val alderVilkår: AlderVilkårDbJson,
    val tiltakDeltagelseVilkår: TiltakDeltagelseVilkårDbJson,
    val kravfristVilkår: KravfristVilkårDbJson,
)

internal fun String.toVilkårssett(
    vurderingsperiode: Periode,
): Vilkårssett {
    try {
        val vilkårssettJson = deserialize<VilkårssettJson>(this)
        return Vilkårssett(
            vurderingsperiode = vurderingsperiode,
            institusjonsoppholdVilkår = vilkårssettJson.institusjonsoppholdVilkår.toDomain(vurderingsperiode),
            kvpVilkår = vilkårssettJson.kvpVilkår.toDomain(vurderingsperiode),
            introVilkår = vilkårssettJson.introVilkår.toDomain(vurderingsperiode),
            livsoppholdVilkår = vilkårssettJson.livsoppholdVilkår.toDomain(vurderingsperiode),
            alderVilkår = vilkårssettJson.alderVilkår.toDomain(vurderingsperiode),
            tiltakDeltagelseVilkår = vilkårssettJson.tiltakDeltagelseVilkår.toDomain(vurderingsperiode),
            kravfristVilkår = vilkårssettJson.kravfristVilkår.toDomain(vurderingsperiode),
        )
    } catch (exception: Exception) {
        throw InvalidParameterException("Det oppstod en feil ved parsing av json: " + exception.message)
    }
}

internal fun Vilkårssett.toDbJson(): String {
    return serialize(
        VilkårssettJson(
            kvpVilkår = kvpVilkår.toDbJson(),
            introVilkår = introVilkår.toDbJson(),
            livsoppholdVilkår = livsoppholdVilkår.toDbJson(),
            institusjonsoppholdVilkår = institusjonsoppholdVilkår.toDbJson(),
            alderVilkår = alderVilkår.toDbJson(),
            kravfristVilkår = kravfristVilkår.toDbJson(),
            tiltakDeltagelseVilkår = tiltakDeltagelseVilkår.toDbJson(),
        ),
    )
}
