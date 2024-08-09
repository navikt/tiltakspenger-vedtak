package no.nav.tiltakspenger.saksbehandling.service.personopplysning

import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger

interface PersonopplysningService {
    fun hent(sakId: SakId): SakPersonopplysninger
}
