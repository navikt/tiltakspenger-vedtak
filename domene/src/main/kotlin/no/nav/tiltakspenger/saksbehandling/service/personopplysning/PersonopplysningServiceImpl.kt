package no.nav.tiltakspenger.saksbehandling.service.personopplysning

import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo

class PersonopplysningServiceImpl(
    private val personopplysningerRepo: PersonopplysningerRepo,
) : PersonopplysningService {
    override fun hent(sakId: SakId): SakPersonopplysninger = personopplysningerRepo.hent(sakId)
}
