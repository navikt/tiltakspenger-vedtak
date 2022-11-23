package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import java.time.LocalDateTime

class Søker private constructor(
    val søkerId: SøkerId,
    val ident: String,  // TODO skal denne ligge her, eller holder det at den ligger i personopplysninger?
    var personopplysninger: Personopplysninger.Søker?,  // TODO her trenger vi kanskje en liste hvis vi vil ha med barn
    var sistEndret: LocalDateTime,
    val opprettet: LocalDateTime,
) {
    constructor(
        ident: String
    ) : this(
        søkerId = randomId(),
        ident = ident,
        personopplysninger = null,
        sistEndret = LocalDateTime.now(),
        opprettet = LocalDateTime.now(),
    )

    fun håndter(hendelse: IdentMottattHendelse) {
        sistEndret = LocalDateTime.now()
    }

    fun håndter(hendelse: PersonopplysningerMottattHendelse) {
        personopplysninger = hendelse.personopplysninger().filterIsInstance<Personopplysninger.Søker>().first()
        sistEndret = LocalDateTime.now()
    }

    companion object {
        fun randomId() = SøkerId.random()

        fun fromDb(
            søkerId: SøkerId,
            ident: String,
            sistEndret: LocalDateTime,
            personopplysninger: Personopplysninger.Søker?,
            opprettet: LocalDateTime
        ) = Søker(
            søkerId = søkerId,
            ident = ident,
            personopplysninger = personopplysninger,
            sistEndret = sistEndret,
            opprettet = opprettet,
        )
    }
}
