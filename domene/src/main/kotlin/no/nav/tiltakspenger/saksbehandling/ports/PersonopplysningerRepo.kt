package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger

interface PersonopplysningerRepo {
    fun hent(sakId: SakId): SakPersonopplysninger
}
