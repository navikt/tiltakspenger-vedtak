package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut

import no.nav.tiltakspenger.vedtak.routes.behandling.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

internal data class LivsoppholdVilkårDTO(
    val delVilkår: List<DelVilkårDTO>,
    val vilkårLovreferanse: LovreferanseDTO,
    val vurderingsperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
)
