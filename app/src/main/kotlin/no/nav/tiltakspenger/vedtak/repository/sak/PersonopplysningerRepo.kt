package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.felles.SakId

interface PersonopplysningerRepo {
    fun hent(sakId: SakId): List<Personopplysninger>
}
