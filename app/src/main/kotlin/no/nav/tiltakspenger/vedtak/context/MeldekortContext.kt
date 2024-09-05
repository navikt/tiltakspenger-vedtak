package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.meldekort.ports.DokumentGateway
import no.nav.tiltakspenger.meldekort.service.HentMeldekortService
import no.nav.tiltakspenger.meldekort.service.IverksettMeldekortService
import no.nav.tiltakspenger.meldekort.service.SendMeldekortTilBeslutterService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortPostgresRepo

/**
 * Åpen så den kan overstyres i test
 */
@Suppress("unused")
internal open class MeldekortContext(
    val meldekortRepo: MeldekortPostgresRepo,
    val journalførMeldekortGateway: DokumentGateway,
    val hentMeldekortService: HentMeldekortService,
    val iverksettMeldekortService: IverksettMeldekortService,
    val sendMeldekortTilBeslutterService: SendMeldekortTilBeslutterService,

) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
            sakService: SakService,
            tilgangsstyringService: TilgangsstyringService,
            dokumentGateway: DokumentGateway,
        ): MeldekortContext {
            val meldekortRepo = MeldekortPostgresRepo(
                sessionFactory = sessionFactory,
            )
            val hentMeldekortService = HentMeldekortService(
                meldekortRepo = meldekortRepo,
                sakService = sakService,
                tilgangsstyringService = tilgangsstyringService,
            )
            val iverksettMeldekortService = IverksettMeldekortService(
                meldekortRepo = meldekortRepo,
                sessionFactory = sessionFactory,
                sakService = sakService,
            )
            val sendMeldekortTilBeslutterService = SendMeldekortTilBeslutterService(meldekortRepo = meldekortRepo)

            return MeldekortContext(
                meldekortRepo = meldekortRepo,
                journalførMeldekortGateway = dokumentGateway,
                hentMeldekortService = hentMeldekortService,
                iverksettMeldekortService = iverksettMeldekortService,
                sendMeldekortTilBeslutterService = sendMeldekortTilBeslutterService,
            )
        }
    }
}
