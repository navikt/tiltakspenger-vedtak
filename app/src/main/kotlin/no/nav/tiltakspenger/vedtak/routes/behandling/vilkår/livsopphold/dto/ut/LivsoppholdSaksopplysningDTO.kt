package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut

import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.LivsoppholdsytelseTypeDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ÅrsakTilEndringDTO

internal data class LivsoppholdSaksopplysningDTO(
    val periodeMedYtelse: PeriodeMedYtelseDTO,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val ytelse: LivsoppholdsytelseTypeDTO,
    val kilde: KildeDto,
)
