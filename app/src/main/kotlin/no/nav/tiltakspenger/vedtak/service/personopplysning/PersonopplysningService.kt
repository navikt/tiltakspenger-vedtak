package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger

interface PersonopplysningService {
    fun hent(sakId: SakId): List<Personopplysninger>
}
