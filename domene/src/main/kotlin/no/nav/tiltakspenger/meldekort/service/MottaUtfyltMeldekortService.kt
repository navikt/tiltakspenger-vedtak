package no.nav.tiltakspenger.meldekort.service

import mu.KotlinLogging
import no.nav.tiltakspenger.meldekort.domene.UtfyltMeldekort
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo

/**
 * Har kun ansvar for å motta et utfylt meldekort og lagre det i databasen.
 * Se også [no.nav.tiltakspenger.utbetaling.service.OpprettUtbetalingsvedtakService] [no.nav.tiltakspenger.utbetaling.service.SendUtbetalingerService]
 */
class MottaUtfyltMeldekortService(
    private val meldekortRepo: MeldekortRepo,
) {
    private val log = KotlinLogging.logger { }

    fun motta(meldekort: UtfyltMeldekort) {
        if (meldekortRepo.hentForMeldekortId(meldekort.id) != null) {
            log.info { "Meldekort med id ${meldekort.id} finnes allerede i databasen. Lagrer ikke på nytt. Returnerer OK." }
            return
        }
        meldekortRepo.lagre(meldekort)
    }
}
