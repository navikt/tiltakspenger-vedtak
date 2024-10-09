package no.nav.tiltakspenger.meldekort.service

import arrow.core.Either
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.meldekort.domene.KanIkkeSendeMeldekortTilBeslutter
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import java.lang.IllegalStateException

/**
 * Genererer meldekort for alle rammevedtak som ikke har meldekort.
 * Dette vil kun gjelde det første meldekortet
 */
class SendMeldekortTilBeslutterService(
    private val tilgangsstyringService: TilgangsstyringService,
    private val personService: PersonService,
    private val meldekortRepo: MeldekortRepo,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun sendMeldekortTilBeslutter(
        kommando: SendMeldekortTilBeslutterKommando,
    ): Either<KanIkkeSendeMeldekortTilBeslutter, Meldekort.UtfyltMeldekort> {
        require(kommando.saksbehandler.isSaksbehandler()) { "Saksbehandler ${kommando.saksbehandler.navIdent} må ha rollen saksbehandler" }

        val sakId = kommando.sakId
        kastHvisIkkeTilgang(kommando.saksbehandler, kommando.meldekortId, kommando.correlationId)
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

    private suspend fun kastHvisIkkeTilgang(
        saksbehandler: Saksbehandler,
        meldekortId: MeldekortId,
        correlationId: CorrelationId,
    ) {
        val fnr = personService.hentFnrForMeldekortId(meldekortId)
        tilgangsstyringService
            .harTilgangTilPerson(
                fnr = fnr,
                roller = saksbehandler.roller,
                correlationId = correlationId,
            ).onLeft {
                throw IkkeFunnetException("Feil ved sjekk av tilgang til person. meldekortId: $meldekortId. CorrelationId: $correlationId")
            }.onRight {
                if (!it) throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til person")
            }
    }
}
