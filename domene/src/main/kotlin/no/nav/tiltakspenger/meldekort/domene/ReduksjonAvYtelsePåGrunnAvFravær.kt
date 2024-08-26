package no.nav.tiltakspenger.meldekort.domene

/**
 * Tiltakspengeforskriften § 10. Reduksjon av ytelse på grunn av fravær
 */
enum class ReduksjonAvYtelsePåGrunnAvFravær {
    IngenReduksjon,

    // Kommentar jah: Dersom denne endres fra nåværende prosent på 75% på denne endres fra en enum til et sealed interface også må prosenten legges inn i denne dataklassen.
    DelvisReduksjon,
    YtelsenFallerBort,
}
