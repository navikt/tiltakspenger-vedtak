package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KVPVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.toDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal class VilkårssettDTO(
    val kvpVilkår: KVPVilkårDTO,
)

internal fun Vilkårssett.toDTO(): VilkårssettDTO {
    return VilkårssettDTO(
        kvpVilkår = kvpVilkår.toDTO(),
    )
}
