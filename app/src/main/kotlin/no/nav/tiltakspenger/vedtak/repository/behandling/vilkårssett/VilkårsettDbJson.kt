package no.nav.tiltakspenger.vedtak.repository.behandling.vilkårssett

import java.security.InvalidParameterException
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.vedtak.db.deserialize
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.KVPVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.kvp.toDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.LivsoppholdVilkårDbJson
import no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold.toDbJson

private val LOG = KotlinLogging.logger {}

/**
 * Har ansvar for å serialisere/deserialisere Vilkårssett til og fra json for lagring i database.
 */
private class VilkårssettJson(
    val kvpVilkår: KVPVilkårDbJson,
    val livsoppholdVilkår: LivsoppholdVilkårDbJson,
)

internal fun String.toVilkårssett(
    vurderingsperiode: Periode,
    saksopplysninger: List<Saksopplysning>,
    vilkårsvurderinger: List<Vurdering>,
    kravdatoSaksopplysninger: KravdatoSaksopplysninger,
    utfallsperioder: List<Utfallsperiode>,
): Vilkårssett {
    try {
        val vilkårssettJson = deserialize<VilkårssettJson>(this)
        return Vilkårssett(
            kvpVilkår = vilkårssettJson.kvpVilkår.toDomain(),
            livsoppholdVilkår = vilkårssettJson.livsoppholdVilkår.toDomain(vurderingsperiode = vurderingsperiode),
            saksopplysninger = saksopplysninger,
            vilkårsvurderinger = vilkårsvurderinger,
            kravdatoSaksopplysninger = kravdatoSaksopplysninger,
            utfallsperioder = utfallsperioder,
        )
    } catch (exception: Exception) {
        exception.printStackTrace()// { "Det oppstod en feil ved parsing av json: " + exception.message }
        throw InvalidParameterException("Det oppstod en feil ved parsing av json: " + exception.message)
    }
}

internal fun Vilkårssett.toDbJson(): String {
    return serialize(
        VilkårssettJson(
            kvpVilkår = kvpVilkår.toDbJson(),
            livsoppholdVilkår = livsoppholdVilkår.toDbJson(),
        ),
    ).also { println(it) }
}
