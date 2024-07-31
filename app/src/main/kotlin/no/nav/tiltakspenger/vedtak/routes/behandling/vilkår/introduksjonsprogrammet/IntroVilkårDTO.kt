package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class IntroVilkårDTO(
    val søknadSaksopplysning: IntroSaksopplysningDTO,
    val avklartSaksopplysning: IntroSaksopplysningDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val samletUtfall: SamletUtfallDTO,
)

internal fun IntroVilkår.toDTO(): IntroVilkårDTO {
    return IntroVilkårDTO(
        søknadSaksopplysning = søknadSaksopplysning.toDTO(IntroKildeDTO.SØKNAD),
        avklartSaksopplysning = avklartSaksopplysning.toDTO(if (avklartSaksopplysning == søknadSaksopplysning) IntroKildeDTO.SØKNAD else IntroKildeDTO.SAKSBEHANDLER),
        vilkårLovreferanse = lovreferanse.toDTO(),
        samletUtfall = this.samletUtfall().toDTO(),
    )
}
