package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.Livsoppholdsytelse

internal enum class LivsoppholdsytelseDto {
    AAP,
    DAGPENGER,
    ;

    fun toDomain(): Livsoppholdsytelse {
        return when (this) {
            AAP -> Livsoppholdsytelse.AAP
            DAGPENGER -> Livsoppholdsytelse.DAGPENGER
        }
    }
}
