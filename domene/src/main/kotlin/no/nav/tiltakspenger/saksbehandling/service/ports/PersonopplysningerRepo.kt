package no.nav.tiltakspenger.saksbehandling.service.ports

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger

interface PersonopplysningerRepo {
    fun hent(sakId: SakId): SakPersonopplysninger
}
