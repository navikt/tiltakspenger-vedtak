package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.meldekort.ports.GenererMeldekortPdfGateway
import no.nav.tiltakspenger.meldekort.ports.JournalførMeldekortGateway
import no.nav.tiltakspenger.saksbehandling.ports.UtbetalingGateway
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo
import no.nav.tiltakspenger.utbetaling.service.HentUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.JournalførUtbetalingsvedtakService
import no.nav.tiltakspenger.utbetaling.service.SendUtbetalingerService
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.EntraIdSystemtokenClient
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtbetalingHttpClient
import no.nav.tiltakspenger.vedtak.repository.utbetaling.UtbetalingsvedtakPostgresRepo

open class UtbetalingContext(
    sessionFactory: SessionFactory,
    genererMeldekortPdfGateway: GenererMeldekortPdfGateway,
    journalførMeldekortGateway: JournalførMeldekortGateway,
    personService: PersonService,
    entraIdSystemtokenClient: EntraIdSystemtokenClient,
) {
    open val utbetalingGateway: UtbetalingGateway by lazy {
        UtbetalingHttpClient(
            baseUrl = Configuration.utbetalingUrl,
            getToken = { entraIdSystemtokenClient.getSystemtoken(Configuration.utbetalingScope) },
        )
    }
    open val utbetalingsvedtakRepo: UtbetalingsvedtakRepo by lazy {
        UtbetalingsvedtakPostgresRepo(
            sessionFactory as PostgresSessionFactory,
        )
    }
    val hentUtbetalingsvedtakService by lazy {
        HentUtbetalingsvedtakService(utbetalingsvedtakRepo)
    }
    val sendUtbetalingerService: SendUtbetalingerService by lazy {
        SendUtbetalingerService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            utbetalingsklient = utbetalingGateway,
        )
    }
    val journalførUtbetalingsvedtakService by lazy {
        JournalførUtbetalingsvedtakService(
            utbetalingsvedtakRepo = utbetalingsvedtakRepo,
            journalførMeldekortGateway = journalførMeldekortGateway,
            genererMeldekortPdfGateway = genererMeldekortPdfGateway,
            personService = personService,
        )
    }
}
