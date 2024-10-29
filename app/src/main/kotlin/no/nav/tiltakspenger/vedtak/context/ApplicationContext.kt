package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.EntraIdSystemtokenClient
import no.nav.tiltakspenger.vedtak.auth.EntraIdSystemtokenHttpClient
import no.nav.tiltakspenger.vedtak.auth2.MicrosoftEntraIdTokenService
import no.nav.tiltakspenger.vedtak.auth2.TokenService

/**
 * Inneholder alle klienter, repoer og servicer.
 * Tanken er at man kan erstatte klienter og repoer med Fakes for testing.
 */
@Suppress("unused")
open class ApplicationContext(
    val sessionFactory: SessionFactory,
    private val gitHash: String,
) {
    open val tokenService: TokenService by lazy {
        val tokenVerificationToken = Configuration.TokenVerificationConfig()
        MicrosoftEntraIdTokenService(
            url = tokenVerificationToken.jwksUri,
            issuer = tokenVerificationToken.issuer,
            clientId = tokenVerificationToken.clientId,
            autoriserteBrukerroller = tokenVerificationToken.roles,
        )
    }
    open val entraIdSystemtokenClient: EntraIdSystemtokenClient by lazy {
        EntraIdSystemtokenHttpClient(
            baseUrl = Configuration.azureOpenidConfigTokenEndpoint,
            clientId = Configuration.clientId,
            clientSecret = Configuration.clientSecret,
        )
    }
    open val personContext by lazy { PersonContext(sessionFactory, entraIdSystemtokenClient) }
    open val dokumentContext by lazy { DokumentContext(entraIdSystemtokenClient) }
    open val statistikkContext by lazy { StatistikkContext(sessionFactory) }
    open val søknadContext by lazy { SøknadContext(sessionFactory) }
    open val tiltakContext by lazy { TiltakContext(entraIdSystemtokenClient) }
    open val sakContext by lazy {
        SakContext(
            sessionFactory = sessionFactory,
            søknadService = søknadContext.søknadService,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            tilgangsstyringService = personContext.tilgangsstyringService,
            tiltakGateway = tiltakContext.tiltakGateway,
            personService = personContext.personService,
            gitHash = gitHash,
        )
    }
    open val utbetalingContext by lazy {
        UtbetalingContext(
            sessionFactory = sessionFactory,
            genererMeldekortPdfGateway = dokumentContext.genererMeldekortPdfGateway,
            journalførMeldekortGateway = dokumentContext.journalførMeldekortGateway,
            entraIdSystemtokenClient = entraIdSystemtokenClient,
            navIdentClient = personContext.navIdentClient,
        )
    }
    open val meldekortContext by lazy {
        MeldekortContext(
            sessionFactory = sessionFactory,
            sakService = sakContext.sakService,
            tilgangsstyringService = personContext.tilgangsstyringService,
            utbetalingsvedtakRepo = utbetalingContext.utbetalingsvedtakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            personService = personContext.personService,
        )
    }
    open val førstegangsbehandlingContext by lazy {
        FørstegangsbehandlingContext(
            sessionFactory = sessionFactory,
            meldekortRepo = meldekortContext.meldekortRepo,
            sakRepo = sakContext.sakRepo,
            statistikkSakRepo = statistikkContext.statistikkSakRepo,
            statistikkStønadRepo = statistikkContext.statistikkStønadRepo,
            gitHash = gitHash,
            journalførVedtaksbrevGateway = dokumentContext.journalførVedtaksbrevGateway,
            genererVedtaksbrevGateway = dokumentContext.genererVedtaksbrevGateway,
            tilgangsstyringService = personContext.tilgangsstyringService,
            dokdistGateway = dokumentContext.dokdistGateway,
            personService = personContext.personService,
            navIdentClient = personContext.navIdentClient,
        )
    }
}
