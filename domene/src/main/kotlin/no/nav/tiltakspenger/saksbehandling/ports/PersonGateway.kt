package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.EnkelPerson
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker

interface PersonGateway {
    suspend fun hentPerson(fnr: Fnr): PersonopplysningerSøker
    suspend fun hentEnkelPerson(fnr: Fnr): EnkelPerson
}
