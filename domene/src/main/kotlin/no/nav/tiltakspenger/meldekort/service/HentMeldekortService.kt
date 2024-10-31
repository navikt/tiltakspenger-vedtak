package no.nav.tiltakspenger.meldekort.service

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService

class HentMeldekortService(
    private val meldekortRepo: MeldekortRepo,
    private val sakService: SakService,
    private val tilgangsstyringService: TilgangsstyringService,
) {
    val logger = KotlinLogging.logger { }

    suspend fun hentForMeldekortId(
        meldekortId: MeldekortId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Meldekort? {
        require(saksbehandler.isSaksbehandler() || saksbehandler.isBeslutter()) { "Saksbehandler ${saksbehandler.navIdent} må ha rollen saksbehandler eller beslutter" }
        kastHvisIkkeTilgangTilMeldekort(saksbehandler, meldekortId, correlationId)
        val meldekort = meldekortRepo.hentForMeldekortId(meldekortId) ?: return null

        return meldekort.also {
            logger.info { "Hentet meldekort med meldekortId $meldekortId. saksbehandler: ${saksbehandler.navIdent}" }
        }
    }

    private suspend fun kastHvisIkkeTilgangTilSak(
        saksbehandler: Saksbehandler,
        sakId: SakId,
        correlationId: CorrelationId,
    ) {
        val fnr = sakService.hentFnrForSakId(sakId) ?: throw IllegalArgumentException("Fant ikke fnr for sakId: $sakId")
        tilgangsstyringService
            .harTilgangTilPerson(
                fnr = fnr,
                roller = saksbehandler.roller,
                correlationId = correlationId,
            ).onLeft {
                throw IkkeFunnetException("Feil ved sjekk av tilgang til person. meldekortId: $sakId. CorrelationId: $correlationId")
            }.onRight {
                if (!it) throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til person")
            }
    }

    private suspend fun kastHvisIkkeTilgangTilMeldekort(
        saksbehandler: Saksbehandler,
        meldekortId: MeldekortId,
        correlationId: CorrelationId,
    ) {
        val fnr = meldekortRepo.hentFnrForMeldekortId(meldekortId) ?: throw IkkeFunnetException("Fant ikke fødselsnummer for meldekortId: $meldekortId")
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
