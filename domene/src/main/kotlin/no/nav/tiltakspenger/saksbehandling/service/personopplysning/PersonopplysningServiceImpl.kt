package no.nav.tiltakspenger.saksbehandling.service.personopplysning

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.service.ports.PersonopplysningerRepo

class PersonopplysningServiceImpl(
    private val personopplysningerRepo: PersonopplysningerRepo,
) : PersonopplysningService {
    override fun hent(sakId: SakId): SakPersonopplysninger {
        return personopplysningerRepo.hent(sakId)
    }
}
