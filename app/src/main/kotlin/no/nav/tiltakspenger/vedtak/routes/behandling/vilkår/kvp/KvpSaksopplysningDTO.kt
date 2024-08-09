package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KvpSaksopplysning

internal data class KvpSaksopplysningDTO(
    val periodeMedDeltagelse: PeriodeMedDeltagelseDTO,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: KildeDTO,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun KvpSaksopplysning.toDTO(kilde: KildeDTO): KvpSaksopplysningDTO =
    KvpSaksopplysningDTO(
        periodeMedDeltagelse =
        this.deltar
            .perioder()
            .tilEnkelPeriode()
            .toDTO(),
        årsakTilEndring =
        when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> KvpSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> KvpSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
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
