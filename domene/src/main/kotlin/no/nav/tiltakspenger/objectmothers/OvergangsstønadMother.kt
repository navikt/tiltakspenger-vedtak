package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.felles.OvergangsstønadVedtakId
import no.nav.tiltakspenger.vedtak.OvergangsstønadPeriode
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
import java.time.LocalDate
import java.time.LocalDateTime

interface OvergangsstønadMother {
    fun overgangsstønadVedtak(
        id: OvergangsstønadVedtakId = OvergangsstønadVedtakId.random(),
        harUføregrad: Boolean = false,
        datoUfør: LocalDate = 1.januar(2022),
        virkDato: LocalDate = 1.januar(2022),
        innhentet: LocalDateTime = 1.januarDateTime(2022),
    ): OvergangsstønadVedtak =
        OvergangsstønadVedtak(
            id = id,
            periode = OvergangsstønadPeriode(fom = "", tom = "", datakilde = ""), innhentet =

        )

    fun overgangsstønadPeriode(
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.januar(2022),
    ): OvergangsstønadPeriode =
        OvergangsstønadPeriode(
            fom = fom,
            tom = tom,
            datakilde = ""
        )
}
