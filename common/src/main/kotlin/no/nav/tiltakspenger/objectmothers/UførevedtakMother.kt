package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.UføreVedtakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.innsending.UføreVedtak
import java.time.LocalDate
import java.time.LocalDateTime

interface UførevedtakMother {
    fun uføreVedtak(
        id: UføreVedtakId = UføreVedtakId.random(),
        harUføregrad: Boolean = false,
        datoUfør: LocalDate = 1.januar(2022),
        virkDato: LocalDate = 1.januar(2022),
        innhentet: LocalDateTime = 1.januarDateTime(2022),
    ): UføreVedtak =
        UføreVedtak(
            id = id,
            harUføregrad = harUføregrad,
            datoUfør = datoUfør,
            virkDato = virkDato,
            innhentet = innhentet,
        )
}
