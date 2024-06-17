package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.PeriodisertUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class KVPVilkårDTO(
    val søknadSaksopplysning: KvpSaksopplysningDTO,
    val saksbehandlerSaksopplysning: KvpSaksopplysningDTO?,
    val avklartSaksopplysning: KvpSaksopplysningDTO,
    val utfallsperioder: List<PeriodisertUtfallDTO>,
    val vilkårLovreferanse: LovreferanseDTO,
)

internal fun KVPVilkår.toDTO(): KVPVilkårDTO {
    return KVPVilkårDTO(
        søknadSaksopplysning = søknadSaksopplysning.toDTO(),
        saksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.toDTO(),
        avklartSaksopplysning = avklartSaksopplysning.toDTO(),
        utfallsperioder = utfall.toDTO(),
        vilkårLovreferanse = lovreferanse.toDTO(),
    )
}
