package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.service.ports.PersonopplysningerRepo

class PersonopplysningServiceImpl(
    private val personopplysningerRepo: PersonopplysningerRepo,
) : PersonopplysningService {
    override fun hent(sakId: SakId): SakPersonopplysninger {
        return personopplysningerRepo.hent(sakId)
    }
}
