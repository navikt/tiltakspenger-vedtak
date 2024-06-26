package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.KVPVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal class VilkårssettDTO(
    val kvpVilkår: KVPVilkårDTO,
)

internal fun Vilkårssett.toDTO(vurderingsperiode: PeriodeDTO): VilkårssettDTO {
    return VilkårssettDTO(
        kvpVilkår = kvpVilkår.toDTO(vurderingsperiode),
    )
}
