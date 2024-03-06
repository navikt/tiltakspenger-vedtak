package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerRepo

class PersonopplysningServiceImpl(
    private val personopplysningerRepo: PersonopplysningerRepo,
) : PersonopplysningService {
    override fun hent(sakId: SakId): List<Personopplysninger> {
        return personopplysningerRepo.hent(sakId)
    }
}
