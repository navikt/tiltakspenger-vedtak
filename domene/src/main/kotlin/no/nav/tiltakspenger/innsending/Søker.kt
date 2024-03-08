package no.nav.tiltakspenger.innsending

import mu.KotlinLogging
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.innsending.meldinger.IdentMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.innsending.meldinger.SkjermingMottattHendelse
import no.nav.tiltakspenger.saksbehandling.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.personopplysninger.søker

private val SECURELOG = KotlinLogging.logger("tjenestekall")

class Søker private constructor(
    val søkerId: SøkerId,
    val ident: String, // TODO skal denne ligge her, eller holder det at den ligger i personopplysninger?
    var personopplysninger: PersonopplysningerSøker?, // TODO her trenger vi kanskje en liste hvis vi vil ha med barn
) {
    constructor(
        ident: String,
    ) : this(
        søkerId = randomId(),
        ident = ident,
        personopplysninger = null,
    )

    fun håndter(hendelse: IdentMottattHendelse) {
        SECURELOG.info { "Søker fikk en identMottattHendelse med iden ${hendelse.ident()} som vi ignorerer" }
    }

    fun håndter(hendelse: PersonopplysningerMottattHendelse) {
        personopplysninger = hendelse.personopplysninger().søker()
    }

    fun håndter(hendelse: SkjermingMottattHendelse) {
        personopplysninger = personopplysninger?.copy(
            skjermet = hendelse.skjerming().søker.skjerming,
        )
    }

    fun sjekkOmSaksbehandlerHarTilgang(saksbehandler: Saksbehandler) {
        fun sjekkBeskyttelsesbehovStrengtFortrolig(harBeskyttelsesbehovStrengtFortrolig: Boolean) {
            if (harBeskyttelsesbehovStrengtFortrolig) {
                SECURELOG.info("erStrengtFortrolig")
                // Merk at vi ikke sjekker egenAnsatt her, strengt fortrolig trumfer det
                if (Rolle.STRENGT_FORTROLIG_ADRESSE in saksbehandler.roller) {
                    SECURELOG.info("Access granted to strengt fortrolig for $ident")
                } else {
                    SECURELOG.info("Access denied to strengt fortrolig for $ident")
                    throw TilgangException("Saksbehandler har ikke tilgang")
                }
            }
        }

        fun sjekkBeskytelsesbehovFortrolig(harBeskyttelsesbehovFortrolig: Boolean) {
            if (harBeskyttelsesbehovFortrolig) {
                SECURELOG.info("erFortrolig")
                // Merk at vi ikke sjekker egenAnsatt her, fortrolig trumfer det
                if (Rolle.FORTROLIG_ADRESSE in saksbehandler.roller) {
                    SECURELOG.info("Access granted to fortrolig for $ident")
                } else {
                    SECURELOG.info("Access denied to fortrolig for $ident")
                    throw TilgangException("Saksbehandler har ikke tilgang")
                }
            }
        }

        fun sjekkBeskyttelsesbehovSkjermet(
            erEgenAnsatt: Boolean,
            harBeskyttelsesbehovFortrolig: Boolean,
            harBeskyttelsesbehovStrengtFortrolig: Boolean,
        ) {
            if (erEgenAnsatt && !(harBeskyttelsesbehovFortrolig || harBeskyttelsesbehovStrengtFortrolig)) {
                SECURELOG.info("erEgenAnsatt")
                // Er kun egenAnsatt, har ikke et beskyttelsesbehov i tillegg
                if (Rolle.SKJERMING in saksbehandler.roller) {
                    SECURELOG.info("Access granted to egen ansatt for $ident")
                } else {
                    SECURELOG.info("Access denied to egen ansatt for $ident")
                    throw TilgangException("Saksbehandler har ikke tilgang")
                }
            }
        }

        fun sjekkSøkerForTilgang(personopplysninger: PersonopplysningerSøker) {
            val harBeskyttelsesbehovFortrolig = personopplysninger.fortrolig
            val harBeskyttelsesbehovStrengtFortrolig =
                personopplysninger.strengtFortrolig || personopplysninger.strengtFortroligUtland
            val erEgenAnsatt = personopplysninger.skjermet ?: false

            sjekkBeskyttelsesbehovStrengtFortrolig(harBeskyttelsesbehovStrengtFortrolig)
            sjekkBeskytelsesbehovFortrolig(harBeskyttelsesbehovFortrolig)
            sjekkBeskyttelsesbehovSkjermet(
                erEgenAnsatt,
                harBeskyttelsesbehovFortrolig,
                harBeskyttelsesbehovStrengtFortrolig,
            )
        }

        personopplysninger?.let { sjekkSøkerForTilgang(it) }
            ?: throw TilgangException("Umulig å vurdere tilgang")
    }

    companion object {
        fun randomId() = SøkerId.random()

        fun fromDb(
            søkerId: SøkerId,
            ident: String,
            personopplysninger: PersonopplysningerSøker?,
        ) = Søker(
            søkerId = søkerId,
            ident = ident,
            personopplysninger = personopplysninger,
        )
    }
}
