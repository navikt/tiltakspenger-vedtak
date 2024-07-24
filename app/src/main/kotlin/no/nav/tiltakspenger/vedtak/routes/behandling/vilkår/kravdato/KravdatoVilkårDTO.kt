package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravdato

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato.KravdatoVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

/**
 * Har ansvar for å serialisere Vilkårssett til json. Kontrakt mot frontend.
 */
internal data class KravdatoVilkårDTO(
    val søknadSaksopplysning: KravdatoSaksopplysningDTO,
    val avklartSaksopplysning: KravdatoSaksopplysningDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val vurderingsperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
)

internal fun KravdatoVilkår.toDTO(vurderingsperiode: PeriodeDTO): KravdatoVilkårDTO {
    return KravdatoVilkårDTO(
        søknadSaksopplysning = søknadSaksopplysning.toDTO(KravdatoKildeDTO.SØKNAD),
        avklartSaksopplysning = avklartSaksopplysning.toDTO(if (avklartSaksopplysning == søknadSaksopplysning) KravdatoKildeDTO.SØKNAD else KravdatoKildeDTO.SAKSBEHANDLER), // TODO Kew her må vi gå opp kilde
        vilkårLovreferanse = lovreferanse.toDTO(),
        vurderingsperiode = vurderingsperiode,
        samletUtfall = this.samletUtfall.toDTO(),
    )
}
