package no.nav.tiltakspenger.vedtak.routes.meldekort.dto

import no.nav.tiltakspenger.meldekort.domene.Meldeperiode
import java.time.LocalDate

data class MeldekortDagDTO(
    val dato: LocalDate,
    val status: String,
    val reduksjonAvYtelsePåGrunnAvFravær: ReduksjonAvYtelsePåGrunnAvFraværDTO?,
)

fun Meldeperiode.toDTO(): List<MeldekortDagDTO> =
    this.map {
        MeldekortDagDTO(
            dato = it.dato,
            status = it.toStatusDTO().toString(),
            reduksjonAvYtelsePåGrunnAvFravær = it.reduksjon?.toDTO(),
        )
    }
