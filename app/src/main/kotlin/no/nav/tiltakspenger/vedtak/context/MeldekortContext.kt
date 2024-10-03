package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.meldekort.service.HentMeldekortService
import no.nav.tiltakspenger.meldekort.service.IverksettMeldekortService
import no.nav.tiltakspenger.meldekort.service.SendMeldekortTilBeslutterService
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.vedtak.repository.meldekort.MeldekortPostgresRepo

/**
 * Åpen så den kan overstyres i test
 */
@Suppress("unused")
open class MeldekortContext(
    sessionFactory: SessionFactory,
    sakService: SakService,
    tilgangsstyringService: TilgangsstyringService,
    utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    statistikkStønadRepo: StatistikkStønadRepo,
) {
    open val meldekortRepo: MeldekortRepo by lazy {
        MeldekortPostgresRepo(
            sessionFactory = sessionFactory as PostgresSessionFactory,
        )
    }
    val hentMeldekortService by lazy {
        HentMeldekortService(
            meldekortRepo = meldekortRepo,
            sakService = sakService,
            tilgangsstyringService = tilgangsstyringService,
        )
    }
    val iverksettMeldekortService by lazy {
        IverksettMeldekortService(
            meldekortRepo = meldekortRepo,
            sessionFactory = sessionFactory,
            sakService = sakService,
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            statistikkStønadRepo = statistikkStønadRepo,

        )
    }
    val sendMeldekortTilBeslutterService by lazy { SendMeldekortTilBeslutterService(meldekortRepo = meldekortRepo) }
}
