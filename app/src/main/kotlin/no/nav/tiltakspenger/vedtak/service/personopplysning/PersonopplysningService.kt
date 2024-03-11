package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.felles.SakId

interface PersonopplysningService {
    fun hent(sakId: SakId): SakPersonopplysninger
}
