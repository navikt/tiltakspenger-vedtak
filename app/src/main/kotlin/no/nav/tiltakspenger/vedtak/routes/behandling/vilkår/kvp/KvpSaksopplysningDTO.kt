package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KvpSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.ÅrsakTilEndring
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

internal data class KvpSaksopplysningDTO(
    val periode: PeriodeDTO,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val kilde: KildeDTO,
) {
    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }
}

internal fun KvpSaksopplysning.toDTO(kilde: KildeDTO): KvpSaksopplysningDTO {
    return KvpSaksopplysningDTO(
        periode = this.deltar.perioder().filter { it.verdi == Deltagelse.DELTAR }.map { it.periode }.single().toDTO(),
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> KvpSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> KvpSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        kilde = kilde,
    )
}
