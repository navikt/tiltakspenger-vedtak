package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.meldinger.IdentMottattHendelse
import java.time.LocalDateTime

class Søker private constructor(
    val søkerId: SøkerId,
    val ident: String,
    var sistEndret: LocalDateTime,
    val opprettet: LocalDateTime,
) {
    constructor(
        ident: String
    ) : this(
        søkerId = randomId(),
        ident = ident,
        sistEndret = LocalDateTime.now(),
        opprettet = LocalDateTime.now()
    )

    fun håndter(hendelse: IdentMottattHendelse) {
        sistEndret = LocalDateTime.now()
    }


    companion object {
        fun randomId() = SøkerId.random()

        fun fromDb(
            søkerId: SøkerId,
            ident: String,
            sistEndret: LocalDateTime,
            opprettet: LocalDateTime
        ) = Søker(søkerId, ident, sistEndret, opprettet)
    }
}
