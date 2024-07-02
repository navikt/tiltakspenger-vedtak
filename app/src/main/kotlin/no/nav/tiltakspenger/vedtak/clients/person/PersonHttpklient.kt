package no.nav.tiltakspenger.vedtak.clients.person

import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklient
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
    override suspend fun hentPerson(ident: String): List<Personopplysninger> {
        val token = azureTokenProvider.getToken()
        return personklient.hentPerson(ident, token).fold(
            // TODO jah: Her har vi mulighet til å returnere Either.left istedet for å kaste.
            ifLeft = { throw RuntimeException("Feil ved henting av personopplysninger for ident $ident") },
            ifRight = { (person, _) -> mapPersonopplysninger(person, LocalDateTime.now(), ident) },
        )
    }
}
