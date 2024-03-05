package no.nav.tiltakspenger.vedtak.service.personopplysning

import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.innsending.Skjerming

interface PersonopplysningService {
    fun hent(sakId: SakId): List<Personopplysninger>
    fun mottaPersonopplysninger(journalpostId: String, personopplysninger: List<Personopplysninger>)
    fun mottaSkjerming(journalpostId: String, skjerming: Skjerming)
}
