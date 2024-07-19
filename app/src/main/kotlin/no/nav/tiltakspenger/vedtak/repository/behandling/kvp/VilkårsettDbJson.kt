package no.nav.tiltakspenger.vedtak.repository.behandling.kvp

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.vedtak.db.deserialize
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.behandling.alder.AlderVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.alder.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet.IntroVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.LivsoppholdVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.toDbJson
import java.security.InvalidParameterException

/**
 * Har ansvar for å serialisere/deserialisere Vilkårssett til og fra json for lagring i database.
 */
private class VilkårssettJson(
    val kvpVilkår: KVPVilkårDbJson,
    val introVilkår: IntroVilkårDbJson,
    val livsoppholdVilkår: LivsoppholdVilkårDbJson,
    val alderVilkår: AlderVilkårDbJson,
)

internal fun String.toVilkårssett(
    saksopplysninger: List<Saksopplysning>,
    vilkårsvurderinger: List<Vurdering>,
    kravdatoSaksopplysninger: KravdatoSaksopplysninger,
    utfallsperioder: List<Utfallsperiode>,
): Vilkårssett {
    try {
        val vilkårssettJson = deserialize<VilkårssettJson>(this)
        return Vilkårssett(
            kvpVilkår = vilkårssettJson.kvpVilkår.toDomain(),
            introVilkår = vilkårssettJson.introVilkår.toDomain(),
            livsoppholdVilkår = vilkårssettJson.livsoppholdVilkår.toDomain(),
            alderVilkår = vilkårssettJson.alderVilkår.toDomain(),
            saksopplysninger = saksopplysninger,
            vilkårsvurderinger = vilkårsvurderinger,
            kravdatoSaksopplysninger = kravdatoSaksopplysninger,
            utfallsperioder = utfallsperioder,
        )
    } catch (exception: Exception) {
        throw InvalidParameterException("Det oppstod en feil ved parsing av json: " + exception.message)
    }
}

internal fun Vilkårssett.toDbJson(): String {
    return serialize(VilkårssettJson(kvpVilkår = kvpVilkår.toDbJson(), introVilkår = introVilkår.toDbJson(), livsoppholdVilkår = livsoppholdVilkår.toDbJson(), alderVilkår = alderVilkår.toDbJson()))
}
