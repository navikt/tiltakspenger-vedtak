package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroSaksopplysning
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.PeriodeMedDeltagelseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp.toDTO

internal data class IntroSaksopplysningDTO(
    val periodeMedDeltagelse: PeriodeMedDeltagelseDTO,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: IntroKildeDTO,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun IntroSaksopplysning.toDTO(kilde: IntroKildeDTO): IntroSaksopplysningDTO {
    return IntroSaksopplysningDTO(
        periodeMedDeltagelse = this.deltar.perioder().tilEnkelPeriode().toDTO(),
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> IntroSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> IntroSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = kilde,
    )
}

internal fun List<PeriodeMedVerdi<Deltagelse>>.tilEnkelPeriode(): PeriodeMedVerdi<Deltagelse> {
    if (this.size > 1) {
        return this.single { it.verdi == Deltagelse.DELTAR }
    }
    return this.single()
}
