package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.felles.UføreVedtakId
import no.nav.tiltakspenger.vedtak.UføreVedtak
import java.time.LocalDate
import java.time.LocalDateTime

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
