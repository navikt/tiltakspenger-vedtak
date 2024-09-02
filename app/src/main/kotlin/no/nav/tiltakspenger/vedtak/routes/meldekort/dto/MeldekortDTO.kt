package no.nav.tiltakspenger.vedtak.routes.meldekort.dto

import no.nav.tiltakspenger.meldekort.domene.Meldekort
import java.time.LocalDate

data class MeldekortDTO(
    val id: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val meldekortDager: List<MeldekortDagDTO>,
    // TODO post-mvp Kew: Må få på antall dager per uke når vi trenger det.
//    val antallDagerPerUke: Int,
)

fun Meldekort.toDTO(): MeldekortDTO =
    MeldekortDTO(
        id = id.toString(),
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
        meldekortDager = meldekortperiode.toDTO(),
    )
