package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltak

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.TiltakVilkårNy
import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

/**
 * Har ansvar for å serialisere Tiltakvilkår til json. Kontrakt mot frontend.
 */
internal data class TiltakVilkårDTO(
    val registerSaksopplysning: TiltakSaksopplysningDTO,
    val avklartSaksopplysning: TiltakSaksopplysningDTO,
    val vilkårLovreferanse: LovreferanseDTO,
    val utfallperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
)

internal fun TiltakVilkårNy.toDTO(): TiltakVilkårDTO {
    return TiltakVilkårDTO(
        registerSaksopplysning = registerSaksopplysning.toDTO(TiltakKildeDTO.SØKNAD),
        avklartSaksopplysning = avklartSaksopplysning.toDTO(if (avklartSaksopplysning == registerSaksopplysning) TiltakKildeDTO.SØKNAD else TiltakKildeDTO.SAKSBEHANDLER),
        vilkårLovreferanse = lovreferanse.toDTO(),
        utfallperiode = this.utfall.totalePeriode.toDTO(),
        samletUtfall = this.samletUtfall.toDTO(),
    )
}
