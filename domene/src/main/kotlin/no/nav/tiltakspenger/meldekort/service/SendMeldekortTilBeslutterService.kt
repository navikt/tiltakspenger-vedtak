package no.nav.tiltakspenger.meldekort.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.meldekort.domene.KanIkkeSendeMeldekortTilBeslutter
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import java.lang.IllegalStateException

/**
 * Genererer meldekort for alle rammevedtak som ikke har meldekort.
 * Dette vil kun gjelde det første meldekortet
 */
class SendMeldekortTilBeslutterService(
    private val meldekortRepo: MeldekortRepo,
) {
    private val logger = KotlinLogging.logger {}

    fun sendMeldekortTilBeslutter(
        kommando: SendMeldekortTilBeslutterKommando,
    ): Either<KanIkkeSendeMeldekortTilBeslutter, Meldekort.UtfyltMeldekort> {
        val sakId = kommando.sakId
        val eksisterendeMeldekort =
            meldekortRepo.hentForSakId(sakId)
                ?: throw IllegalStateException("Det finnes ingen eksisterende meldekort for sak $sakId")
        return eksisterendeMeldekort
            .sendTilBeslutter(kommando)
            .map { it.second }
            .onRight {
                meldekortRepo.oppdater(it)
                logger.info { "Meldekort med id ${it.id} sendt til beslutter." }
            }
    }
}
