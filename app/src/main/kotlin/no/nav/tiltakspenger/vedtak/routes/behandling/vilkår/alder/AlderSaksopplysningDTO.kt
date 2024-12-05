package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.alder

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.felles.ÅrsakTilEndringDTO
import java.time.LocalDate

internal data class AlderSaksopplysningDTO(
    val fødselsdato: LocalDate,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: AlderKildeDTO,
)

internal fun AlderSaksopplysning.toDTO(kilde: AlderKildeDTO): AlderSaksopplysningDTO =
    AlderSaksopplysningDTO(
        fødselsdato = fødselsdato,
        årsakTilEndring =
        when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = kilde,
    )

internal fun List<PeriodeMedVerdi<Deltagelse>>.tilEnkelPeriode(): PeriodeMedVerdi<Deltagelse> {
    if (this.size > 1) {
        return this.single { it.verdi == Deltagelse.DELTAR }
    }
    return this.single()
}
