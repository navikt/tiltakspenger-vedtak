package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.AarsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.Livsoppholdsytelse
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class LivsoppholdSaksopplysningDto(
    val periodeMedYtelse: PeriodeMedYtelseDto,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val ytelse: LivsoppholdsytelseDto,
    val kilde: KildeDto,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun LivsoppholdSaksopplysning.toDTO(kilde: KildeDto): LivsoppholdSaksopplysningDto {
    return LivsoppholdSaksopplysningDto(
        periodeMedYtelse = this.harYtelse.perioder().tilEnkelPeriode().toDTO(),
        årsakTilEndring = when (årsakTilEndring) {
            AarsakTilEndring.FEIL_I_INNHENTET_DATA -> LivsoppholdSaksopplysningDto.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            AarsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> LivsoppholdSaksopplysningDto.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = kilde,
        ytelse = this.livsoppholdsytelse.toDTO(),
    )
}

internal fun List<PeriodeMedVerdi<HarYtelse>>.tilEnkelPeriode(): PeriodeMedVerdi<HarYtelse> {
    if (this.size > 1) {
        return this.single { it.verdi == HarYtelse.HAR_YTELSE }
    }
    return this.single()
}

fun PeriodeMedVerdi<HarYtelse>.toDTO(): PeriodeMedYtelseDto {
    return PeriodeMedYtelseDto(
        periode = this.periode.toDTO(),
        harYtelse = this.verdi.toDTO(),
    )
}

fun HarYtelse.toDTO(): HarYtelseDto {
    return HarYtelseDto.valueOf(this.name)
}

internal fun Livsoppholdsytelse.toDTO(): LivsoppholdsytelseDto {
    return LivsoppholdsytelseDto.valueOf(this.name)
}
