package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.felles.SakId

interface PersonopplysningerRepo {
    fun hent(sakId: SakId): List<Personopplysninger>
    fun lagre(sakId: SakId, personopplysninger: List<Personopplysninger>)
}
