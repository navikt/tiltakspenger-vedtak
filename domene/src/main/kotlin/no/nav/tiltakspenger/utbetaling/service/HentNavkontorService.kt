package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.utbetaling.ports.NavkontorGateway

class HentNavkontorService(
    private val navkontorGateway: NavkontorGateway,
    private val personService: PersonService,
) {
    fun hentNavkontorForFnr(fnr: Fnr): String {
        tilgangsstyringService.
        return "navkontor"
    }
}
