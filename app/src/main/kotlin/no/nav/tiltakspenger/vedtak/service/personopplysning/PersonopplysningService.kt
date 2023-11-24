package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.felles.SakId

interface PersonopplysningService {
    fun hent(sakId: SakId): List<Personopplysninger>
}
