package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.kvp

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KvpSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.ÅrsakTilEndring
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.SaksbehandlerDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDto

internal data class KvpSaksopplysningDTO(
    val deltakelseForPeriode: List<PeriodiseringAvDeltagelseDTO>,
    val årsakTilEndring: ÅrsakTilEndringDTO?,
    val saksbehandler: SaksbehandlerDTO?,
    val tidsstempel: String,
) {

    data class PeriodiseringAvDeltagelseDTO(
        val periode: PeriodeDTO,
        val deltar: DeltagelseDTO,
    )

    enum class ÅrsakTilEndringDTO {
        FEIL_I_INNHENTET_DATA,
        ENDRING_ETTER_SØKNADSTIDSPUNKT,
    }

    enum class DeltagelseDTO {
        DELTAR,
        DELTAR_IKKE,
    }
}

internal fun KvpSaksopplysning.toDTO(): KvpSaksopplysningDTO {
    return KvpSaksopplysningDTO(
        deltakelseForPeriode = this.deltar.perioder().map {
            KvpSaksopplysningDTO.PeriodiseringAvDeltagelseDTO(
                periode = it.periode.toDto(),
                deltar = when (it.verdi) {
                    Deltagelse.DELTAR -> KvpSaksopplysningDTO.DeltagelseDTO.DELTAR
                    Deltagelse.DELTAR_IKKE -> KvpSaksopplysningDTO.DeltagelseDTO.DELTAR_IKKE
                },
            )
        },
        årsakTilEndring = when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> KvpSaksopplysningDTO.ÅrsakTilEndringDTO.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> KvpSaksopplysningDTO.ÅrsakTilEndringDTO.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        saksbehandler = saksbehandler?.toDto(),
        tidsstempel = tidsstempel.toString(),
    )
}
