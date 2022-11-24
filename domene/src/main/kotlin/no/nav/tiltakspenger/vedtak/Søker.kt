package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse

class Søker private constructor(
    val søkerId: SøkerId,
    val ident: String,  // TODO skal denne ligge her, eller holder det at den ligger i personopplysninger?
    var personopplysninger: Personopplysninger.Søker?,  // TODO her trenger vi kanskje en liste hvis vi vil ha med barn
) {
    constructor(
        ident: String
    ) : this(
        søkerId = randomId(),
        ident = ident,
        personopplysninger = null,
    )

    fun håndter(hendelse: IdentMottattHendelse) {
        // her skjer det ikke en pøkk...
    }

    fun håndter(hendelse: PersonopplysningerMottattHendelse) {
        personopplysninger = hendelse.personopplysninger().filterIsInstance<Personopplysninger.Søker>().first()
    }

    fun håndter(hendelse: SkjermingMottattHendelse) {
        personopplysninger = personopplysninger?.copy(
            skjermet = hendelse.skjerming().skjerming
        )
    }

    companion object {
        fun randomId() = SøkerId.random()

        fun fromDb(
            søkerId: SøkerId,
            ident: String,
            personopplysninger: Personopplysninger.Søker?,
        ) = Søker(
            søkerId = søkerId,
            ident = ident,
            personopplysninger = personopplysninger,
        )
    }
}
