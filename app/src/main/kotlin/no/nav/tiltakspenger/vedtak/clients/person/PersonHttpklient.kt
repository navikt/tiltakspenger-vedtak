package no.nav.tiltakspenger.vedtak.clients.person

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklient
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.AdressebeskyttelseKunneIkkeAvklares
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.DeserializationException
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.FantIkkePerson
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.FødselKunneIkkeAvklares
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.Ikke2xx
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.IngenNavnFunnet
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.NavnKunneIkkeAvklares
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.NetworkError
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.ResponsManglerPerson
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.UkjentFeil
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import java.time.LocalDateTime

internal class PersonHttpklient(
    endepunkt: String,
    private val azureTokenProvider: AzureTokenProvider,
) : PersonGateway {
    private val personklient = FellesPersonklient.create(
        endepunkt = endepunkt,
    )

    /**
     * Benytter seg av [AzureTokenProvider] for å hente token for å hente personopplysninger vha. systembruker.
     * TODO jah: Dersom vi ønsker og sende saksbehandler sitt OBO-token, kan vi lage en egen metode for dette.
     */
    override suspend fun hentPerson(fnr: Fnr): List<Personopplysninger> {
        val token = azureTokenProvider.getToken()
        return personklient.hentPerson(fnr, token).fold(
            // TODO jah: Her har vi mulighet til å returnere Either.left istedet for å kaste.
            ifLeft = {
                when (it) {
                    is AdressebeskyttelseKunneIkkeAvklares -> throw RuntimeException("Feil ved henting av personopplysninger: AdressebeskyttelseKunneIkkeAvklares")
                    is DeserializationException -> throw RuntimeException(
                        "Feil ved henting av personopplysninger: DeserializationException",
                        it.exception,
                    )

                    is FantIkkePerson -> throw RuntimeException("Feil ved henting av personopplysninger: FantIkkePerson")
                    is FødselKunneIkkeAvklares -> throw RuntimeException("Feil ved henting av personopplysninger: FødselKunneIkkeAvklares")
                    is Ikke2xx -> throw RuntimeException("Feil ved henting av personopplysninger: $it")
                    is IngenNavnFunnet -> throw RuntimeException("Feil ved henting av personopplysninger: IngenNavnFunnet")
                    is NavnKunneIkkeAvklares -> throw RuntimeException("Feil ved henting av personopplysninger: NavnKunneIkkeAvklares")
                    is NetworkError -> throw RuntimeException(
                        "Feil ved henting av personopplysninger: NetworkError",
                        it.exception,
                    )

                    is ResponsManglerPerson -> throw RuntimeException("Feil ved henting av personopplysninger: ResponsManglerPerson")
                    is UkjentFeil -> throw RuntimeException("Feil ved henting av personopplysninger: $it")
                }
            },
            ifRight = { (person, _) -> mapPersonopplysninger(person, LocalDateTime.now(), fnr) },
        )
    }
}
