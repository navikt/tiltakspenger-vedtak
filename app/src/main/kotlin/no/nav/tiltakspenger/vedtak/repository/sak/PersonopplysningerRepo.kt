package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger

interface PersonopplysningerRepo {
    fun hent(sakId: SakId): List<Personopplysninger>
}
