package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.felles.SakId

interface PersonopplysningService {
    fun hent(sakId: SakId): List<Personopplysninger>
}
