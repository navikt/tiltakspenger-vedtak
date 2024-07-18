package no.nav.tiltakspenger.vedtak.repository.behandling.felles

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.vedtak.db.deserialize
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.behandling.institusjonsopphold.InstitusjonsoppholdVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.institusjonsopphold.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.KVPVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet.IntroVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.introduksjonsprogrammet.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.LivsoppholdVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.toDbJson
import java.security.InvalidParameterException

/**
 * Har ansvar for å serialisere/deserialisere Vilkårssett til og fra json for lagring i database.
 */
private class VilkårssettJson(
    val institusjonsoppholdVilkår: InstitusjonsoppholdVilkårDbJson,
    val kvpVilkår: KVPVilkårDbJson,
    val introVilkår: IntroVilkårDbJson,
    val livsoppholdVilkår: LivsoppholdVilkårDbJson,
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
            institusjonsoppholdVilkår = vilkårssettJson.institusjonsoppholdVilkår.toDomain(),
            kvpVilkår = vilkårssettJson.kvpVilkår.toDomain(),
            introVilkår = vilkårssettJson.introVilkår.toDomain(),
            livsoppholdVilkår = vilkårssettJson.livsoppholdVilkår.toDomain(),
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
    return serialize(VilkårssettJson(kvpVilkår = kvpVilkår.toDbJson(), introVilkår = introVilkår.toDbJson(), livsoppholdVilkår = livsoppholdVilkår.toDbJson(), institusjonsoppholdVilkår = institusjonsoppholdVilkår.toDbJson()))
}
