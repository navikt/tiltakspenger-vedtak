package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.HarYtelse

internal enum class HarYtelseDbJson {
    HAR_YTELSE,
    HAR_IKKE_YTELSE,
    ;

    fun toDomain(): HarYtelse {
        return when (this) {
            HAR_YTELSE -> HarYtelse.HAR_YTELSE
            HAR_IKKE_YTELSE -> HarYtelse.HAR_IKKE_YTELSE
        }
    }
}
