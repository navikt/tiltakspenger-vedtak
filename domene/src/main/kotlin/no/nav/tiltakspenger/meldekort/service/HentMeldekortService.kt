package no.nav.tiltakspenger.meldekort.service

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.MeldekortSammendrag
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService

class HentMeldekortService(
    private val meldekortRepo: MeldekortRepo,
    private val sakService: SakService,
    private val tilgangsstyringService: TilgangsstyringService,
) {
    val logger = KotlinLogging.logger { }

    fun hentForMeldekortId(
        meldekortId: MeldekortId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Meldekort? {
        val fnr = meldekortRepo.hentFnrForMeldekortId(meldekortId) ?: return null

        val meldekort = meldekortRepo.hentForMeldekortId(meldekortId) ?: return null
        kastHvisIkkeTilgang(fnr, saksbehandler, meldekort.sakId, correlationId)

        return meldekort.also {
            logger.info { "Hentet meldekort med meldekortId $meldekortId. saksbehandler: ${saksbehandler.navIdent}" }
        }
    }

    fun hentForSakId(
        sakId: SakId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): List<MeldekortSammendrag> {
        val fnr = sakService.hentFnrForSakId(sakId) ?: throw IllegalArgumentException("Fant ikke fnr for sakId: $sakId")
        kastHvisIkkeTilgang(fnr, saksbehandler, sakId, correlationId)
        return meldekortRepo.hentSammendragforSakId(sakId)
    }

    private fun kastHvisIkkeTilgang(
        fnr: Fnr,
        saksbehandler: Saksbehandler,
        sakId: SakId,
        correlationId: CorrelationId,
    ) {
        runBlocking {
            tilgangsstyringService
                .harTilgangTilPerson(
                    fnr = fnr,
                    roller = saksbehandler.roller,
                    correlationId = correlationId,
                ).onLeft {
                    throw IllegalArgumentException(
                        "Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til person. sakId: $sakId",
                    )
                }.onRight {
                    require(saksbehandler.roller.harSaksbehandlerEllerBehandler()) {
                        "Kan ikke hente meldekort. Saksbehandler ${saksbehandler.navIdent} må ha rollen SAKSBEHANDLER/BESLUTTER. sakId: $sakId"
                    }
                }
        }
    }
}
