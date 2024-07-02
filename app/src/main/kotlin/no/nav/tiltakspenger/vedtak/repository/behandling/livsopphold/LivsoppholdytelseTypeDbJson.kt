package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdsytelseType

enum class LivsoppholdsytelseTypeDbJson {
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
        return when (this) {
            AAP -> LivsoppholdsytelseType.AAP
            DAGPENGER -> LivsoppholdsytelseType.DAGPENGER
            ALDERSPENSJON -> LivsoppholdsytelseType.ALDERSPENSJON
            GJENLEVENDEPENSJON -> LivsoppholdsytelseType.GJENLEVENDEPENSJON
            SYKEPENGER -> LivsoppholdsytelseType.SYKEPENGER
            JOBBSJANSEN -> LivsoppholdsytelseType.JOBBSJANSEN
            FORELDREPENGER -> LivsoppholdsytelseType.FORELDREPENGER
            OMSORGSPENGER -> LivsoppholdsytelseType.OMSORGSPENGER
            OPPLÆRINGSPENGER -> LivsoppholdsytelseType.OPPLÆRINGSPENGER
            OVERGANGSSTØNAD -> LivsoppholdsytelseType.OVERGANGSSTØNAD
            PENSJONSINNTEKT -> LivsoppholdsytelseType.PENSJONSINNTEKT
            PLEIEPENGER_NÆRSTÅENDE -> LivsoppholdsytelseType.PLEIEPENGER_NÆRSTÅENDE
            PLEIEPENGER_SYKTBARN -> LivsoppholdsytelseType.PLEIEPENGER_SYKTBARN
            SUPPLERENDESTØNAD_ALDER -> LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER
            SUPPLERENDESTØNAD_FLYKTNING -> LivsoppholdsytelseType.SUPPLERENDESTØNAD_FLYKTNING
            SVANGERSKAPSPENGER -> LivsoppholdsytelseType.SVANGERSKAPSPENGER
            UFØRETRYGD -> LivsoppholdsytelseType.UFØRETRYGD
        }
    }
}

internal fun LivsoppholdsytelseType.toDbJson(): LivsoppholdsytelseTypeDbJson {
    return when (this) {
        LivsoppholdsytelseType.AAP -> LivsoppholdsytelseTypeDbJson.AAP
        LivsoppholdsytelseType.DAGPENGER -> LivsoppholdsytelseTypeDbJson.DAGPENGER
        LivsoppholdsytelseType.ALDERSPENSJON -> LivsoppholdsytelseTypeDbJson.ALDERSPENSJON
        LivsoppholdsytelseType.GJENLEVENDEPENSJON -> LivsoppholdsytelseTypeDbJson.GJENLEVENDEPENSJON
        LivsoppholdsytelseType.SYKEPENGER -> LivsoppholdsytelseTypeDbJson.SYKEPENGER
        LivsoppholdsytelseType.JOBBSJANSEN -> LivsoppholdsytelseTypeDbJson.JOBBSJANSEN
        LivsoppholdsytelseType.FORELDREPENGER -> LivsoppholdsytelseTypeDbJson.FORELDREPENGER
        LivsoppholdsytelseType.OMSORGSPENGER -> LivsoppholdsytelseTypeDbJson.OMSORGSPENGER
        LivsoppholdsytelseType.OPPLÆRINGSPENGER -> LivsoppholdsytelseTypeDbJson.OPPLÆRINGSPENGER
        LivsoppholdsytelseType.OVERGANGSSTØNAD -> LivsoppholdsytelseTypeDbJson.OVERGANGSSTØNAD
        LivsoppholdsytelseType.PENSJONSINNTEKT -> LivsoppholdsytelseTypeDbJson.PENSJONSINNTEKT
        LivsoppholdsytelseType.PLEIEPENGER_NÆRSTÅENDE -> LivsoppholdsytelseTypeDbJson.PLEIEPENGER_NÆRSTÅENDE
        LivsoppholdsytelseType.PLEIEPENGER_SYKTBARN -> LivsoppholdsytelseTypeDbJson.PLEIEPENGER_SYKTBARN
        LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER -> LivsoppholdsytelseTypeDbJson.SUPPLERENDESTØNAD_ALDER
        LivsoppholdsytelseType.SUPPLERENDESTØNAD_FLYKTNING -> LivsoppholdsytelseTypeDbJson.SUPPLERENDESTØNAD_FLYKTNING
        LivsoppholdsytelseType.SVANGERSKAPSPENGER -> LivsoppholdsytelseTypeDbJson.SVANGERSKAPSPENGER
        LivsoppholdsytelseType.UFØRETRYGD -> LivsoppholdsytelseTypeDbJson.UFØRETRYGD
    }
}
