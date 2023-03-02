package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import java.time.LocalDate
import java.time.LocalDateTime

interface OvergangsstønadMother {
    fun overgangsstønadVedtak(
        id: OvergangsstønadVedtakId = OvergangsstønadVedtakId.random(),
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.januar(2022),
        datakilde: String = "Kilde",
        innhentet: LocalDateTime = 1.januarDateTime(2022),
    ): OvergangsstønadVedtak =
        OvergangsstønadVedtak(
            id = id,
            fom = fom,
            tom = tom,
            datakilde = datakilde,
            innhentet = innhentet,
        )
}
