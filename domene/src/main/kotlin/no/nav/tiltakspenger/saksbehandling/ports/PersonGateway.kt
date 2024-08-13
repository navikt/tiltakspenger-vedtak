package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger

interface PersonGateway {
    suspend fun hentPerson(fnr: Fnr): List<Personopplysninger>
}
