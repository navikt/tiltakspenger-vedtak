package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class InstitusjonsoppholdVilkårDTO(
    val søknadSaksopplysning: InstitusjonsoppholdSaksopplysningDTO,
    val avklartSaksopplysning: InstitusjonsoppholdSaksopplysningDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val samletUtfall: SamletUtfallDTO,
)

internal fun InstitusjonsoppholdVilkår.toDTO(): InstitusjonsoppholdVilkårDTO {
    return InstitusjonsoppholdVilkårDTO(
        søknadSaksopplysning = søknadSaksopplysning.toDTO(KildeDTO.SØKNAD),
        avklartSaksopplysning = avklartSaksopplysning.toDTO(if (avklartSaksopplysning == søknadSaksopplysning) KildeDTO.SØKNAD else KildeDTO.SAKSBEHANDLER),
        vilkårLovreferanse = lovreferanse.toDTO(),
        samletUtfall = this.samletUtfall.toDTO(),
    )
}
