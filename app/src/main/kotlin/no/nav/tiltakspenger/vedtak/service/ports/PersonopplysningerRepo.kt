package no.nav.tiltakspenger.vedtak.service.ports

import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.felles.SakId

interface PersonopplysningerRepo {
    fun hent(sakId: SakId): SakPersonopplysninger
}
