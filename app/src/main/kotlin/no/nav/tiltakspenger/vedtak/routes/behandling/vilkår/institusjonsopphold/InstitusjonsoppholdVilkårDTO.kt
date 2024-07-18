package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.institusjonsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class InstitusjonsoppholdVilkårDTO(
    val søknadSaksopplysning: InstitusjonsoppholdSaksopplysningDTO,
    val avklartSaksopplysning: InstitusjonsoppholdSaksopplysningDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val vurderingsperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
)

internal fun InstitusjonsoppholdVilkår.toDTO(vurderingsperiode: PeriodeDTO): InstitusjonsoppholdVilkårDTO {
    return InstitusjonsoppholdVilkårDTO(
        søknadSaksopplysning = søknadSaksopplysning.toDTO(KildeDTO.SØKNAD),
        avklartSaksopplysning = avklartSaksopplysning.toDTO(if (avklartSaksopplysning == søknadSaksopplysning) KildeDTO.SØKNAD else KildeDTO.SAKSBEHANDLER),
        vilkårLovreferanse = lovreferanse.toDTO(),
        vurderingsperiode = vurderingsperiode,
        samletUtfall = this.samletUtfall.toDTO(),
    )
}
