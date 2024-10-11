package no.nav.tiltakspenger.vedtak.clients.norg

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.GeografiskOmråde
import no.nav.tiltakspenger.felles.Navkontor
import no.nav.tiltakspenger.utbetaling.ports.KunneIkkeHenteNavkontor
import no.nav.tiltakspenger.vedtak.db.deserialize

private data class NavkontorResponseJson(
    val enhetId: Long? = null,
    val navn: String,
    val enhetNr: String,
    val antallRessurser: Int? = null,
    val status: String?,
    val orgNivaa: String? = null,
    val type: String?,
    val organisasjonsnummer: String? = null,
    val underEtableringDato: String,
    val aktiveringsdato: String,
    val underAvviklingDato: String? = null,
    val nedleggelsesdato: String? = null,
    val oppgavebehandler: Boolean? = null,
    val versjon: Int? = null,
    val sosialeTjenester: String? = null,
    val kanalstrategi: String? = null,
    val orgNrTilKommunaltNavKontor: String? = null,
)

private val log = KotlinLogging.logger { }

fun String.toNavkontor(
    geografiskOmråde: GeografiskOmråde,
    httpStatus: Int,
): Either<KunneIkkeHenteNavkontor, Navkontor> {
    return Either.catch {
        deserialize<NavkontorResponseJson>(this)
    }.mapLeft {
        log.error(it) { "Kunne ikke deserialisere NavkontorResponseJson. Status: $httpStatus. Geografisk område: $geografiskOmråde. Response: $this" }
        KunneIkkeHenteNavkontor
    }.map {
        log.debug { "Hentet navkontor for geografisk område $geografiskOmråde. Status: $httpStatus. Geografisk område: $geografiskOmråde. Response: $this" }
        // TODO pre-mvp jah: Vi må verifisere om det er riktig å bruke enhetNr som 'brukersNavkontor'. Dvs. det vi sender til helved utsjekk.
        Navkontor(it.enhetNr)
    }
}
