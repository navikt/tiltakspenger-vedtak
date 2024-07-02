package no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.dto

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType

internal enum class LivsoppholdsytelseTypeDTO {
    AAP,
    DAGPENGER,
    ALDERSPENSJON,
    GJENLEVENDEPENSJON,
    SYKEPENGER,
    JOBBSJANSEN,
    FORELDREPENGER,
    OMSORGSPENGER,
    OPPLÆRINGSPENGER,
    OVERGANGSSTØNAD,
    PENSJONSINNTEKT,
    PLEIEPENGER_NÆRSTÅENDE,
    PLEIEPENGER_SYKTBARN,
    SUPPLERENDESTØNAD_ALDER,
    SUPPLERENDESTØNAD_FLYKTNING,
    SVANGERSKAPSPENGER,
    UFØRETRYGD,
    ;

    fun toDomain(): LivsoppholdsytelseType {
        return LivsoppholdsytelseType.valueOf(this.name)
    }
}
