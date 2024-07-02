package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.ÅrsakTilEndring
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.LivsoppholdsytelseTypeDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut.DelVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut.HarYtelseDto
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut.KildeDto
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut.LivsoppholdSaksopplysningDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut.LivsoppholdVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ut.PeriodeMedYtelseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto.ÅrsakTilEndringDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

object LivsoppholdVilkårMapper {

    internal fun toDTO(livsoppholdVilkår: LivsoppholdVilkår): LivsoppholdVilkårDTO {
        return LivsoppholdVilkårDTO(
            delVilkår = livsoppholdVilkår.delVilkår.map { it.value.toDTO() },
            vilkårLovreferanse = livsoppholdVilkår.lovreferanse.toDTO(),
            vurderingsperiode = livsoppholdVilkår.vurderingsperiode.toDTO(),
            samletUtfall = livsoppholdVilkår.samletUtfall.toDTO(),
        )
    }

    private fun manglendeSaksopplysning(
        vurderingsperiode: Periode,
        livsoppholdytelseType: LivsoppholdsytelseType,
    ): LivsoppholdSaksopplysningDTO =
        LivsoppholdSaksopplysningDTO(
            periodeMedYtelse = PeriodeMedYtelseDTO(
                vurderingsperiode.toDTO(),
                HarYtelseDto.MANGLER,
            ),
            årsakTilEndring = null,
            ytelse = livsoppholdytelseType.toDTO(),
            kilde = KildeDto.SAKSBEHANDLER,
        )

    private fun LivsoppholdDelVilkår.toDTO(): DelVilkårDTO {
        return DelVilkårDTO(
            livsoppholdsytelseType = livsoppholdytelseType.toDTO(),
            avklartSaksopplysning = avklartSaksopplysning()?.toDTO(KildeDto.SAKSBEHANDLER)
                ?: manglendeSaksopplysning(vurderingsperiode, livsoppholdytelseType),
            vurderingsperiode = vurderingsperiode.toDTO(),
            samletUtfall = samletUtfall().toDTO(),
        )
    }

    private fun LivsoppholdSaksopplysning.toDTO(kilde: KildeDto): LivsoppholdSaksopplysningDTO {
        return LivsoppholdSaksopplysningDTO(
            periodeMedYtelse = this.harYtelse.perioder().tilEnkelPeriode().toDTO(),
            årsakTilEndring = when (årsakTilEndring) {
                ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
                ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
                null -> null
            },
            kilde = kilde,
            ytelse = this.livsoppholdsytelseType.toDTO(),
        )
    }

    private fun List<PeriodeMedVerdi<HarYtelse>>.tilEnkelPeriode(): PeriodeMedVerdi<HarYtelse> {
        if (this.size > 1) {
            return this.single { it.verdi == HarYtelse.HAR_YTELSE }
        }
        return this.single()
    }

    private fun PeriodeMedVerdi<HarYtelse>.toDTO(): PeriodeMedYtelseDTO {
        return PeriodeMedYtelseDTO(
            periode = this.periode.toDTO(),
            harYtelse = this.verdi.toDTO(),
        )
    }

    private fun HarYtelse.toDTO(): HarYtelseDto {
        return HarYtelseDto.valueOf(this.name)
    }

    private fun LivsoppholdsytelseType.toDTO(): LivsoppholdsytelseTypeDTO {
        return LivsoppholdsytelseTypeDTO.valueOf(this.name)
    }
}
