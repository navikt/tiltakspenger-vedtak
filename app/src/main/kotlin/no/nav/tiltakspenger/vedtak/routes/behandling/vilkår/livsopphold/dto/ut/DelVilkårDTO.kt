package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut

import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.LivsoppholdsytelseTypeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

internal data class DelVilkårDTO(
    val livsoppholdsytelseType: LivsoppholdsytelseTypeDTO,
    val avklartSaksopplysning: LivsoppholdSaksopplysningDTO,
    val vurderingsperiode: PeriodeDTO,
    val samletUtfall: SamletUtfallDTO,
)
