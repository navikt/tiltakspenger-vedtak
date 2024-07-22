package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class AlderVilkårDTO(
    val søknadSaksopplysning: AlderSaksopplysningDTO,
    val avklartSaksopplysning: AlderSaksopplysningDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val vurderingsperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
)

internal fun AlderVilkår.toDTO(vurderingsperiode: PeriodeDTO): AlderVilkårDTO {
    return AlderVilkårDTO(
        søknadSaksopplysning = søknadSaksopplysning.toDTO(AlderKildeDTO.SØKNAD),
        avklartSaksopplysning = avklartSaksopplysning.toDTO(if (avklartSaksopplysning == søknadSaksopplysning) AlderKildeDTO.SØKNAD else AlderKildeDTO.SAKSBEHANDLER), // TODO Kew her må vi gå opp kilde
        vilkårLovreferanse = lovreferanse.toDTO(),
        vurderingsperiode = vurderingsperiode,
        samletUtfall = this.samletUtfall.toDTO(),
    )
}
