package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.tiltak

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.TiltakSaksopplysning
import java.time.LocalDateTime

internal data class TiltakSaksopplysningDTO(
    val tiltak: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: TiltakKildeDTO,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun TiltakSaksopplysning.toDTO(kilde: TiltakKildeDTO): TiltakSaksopplysningDTO {
    return TiltakSaksopplysningDTO(
        tiltak = tiltak,
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> TiltakSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> TiltakSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
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
