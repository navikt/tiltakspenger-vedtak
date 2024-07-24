package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kravdato

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato.KravdatoSaksopplysning
import java.time.LocalDateTime

internal data class KravdatoSaksopplysningDTO(
    val kravdato: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: KravdatoKildeDTO,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun KravdatoSaksopplysning.toDTO(kilde: KravdatoKildeDTO): KravdatoSaksopplysningDTO {
    return KravdatoSaksopplysningDTO(
        kravdato = kravdato,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> KravdatoSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> KravdatoSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
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
