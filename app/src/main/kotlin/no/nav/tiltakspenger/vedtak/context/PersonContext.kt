package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.libs.personklient.pdl.TilgangsstyringService
import no.nav.tiltakspenger.libs.personklient.tilgangsstyring.TilgangsstyringServiceImpl
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonRepo
import no.nav.tiltakspenger.saksbehandling.ports.PoaoTilgangGateway
import no.nav.tiltakspenger.saksbehandling.service.person.PersonService
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.person.PersonHttpklient
import no.nav.tiltakspenger.vedtak.clients.poaotilgang.PoaoTilgangClient
import no.nav.tiltakspenger.vedtak.repository.person.PersonPostgresRepo

@Suppress("unused")
open class PersonContext(
    sessionFactory: SessionFactory,
) {
    val tokenProviderPdl by lazy { AzureTokenProvider(config = Configuration.ouathConfigPdl()) }

    open val personGateway: PersonGateway by lazy {
        PersonHttpklient(
            endepunkt = Configuration.pdlClientConfig().baseUrl,
            azureTokenProvider = tokenProviderPdl,
        )
    }
    open val tilgangsstyringService: TilgangsstyringService by lazy {
        TilgangsstyringServiceImpl.create(
            skjermingBaseUrl = Configuration.skjermingClientConfig().baseUrl,
            getPdlPipToken = tokenProviderPdl::getToken,
            pdlPipUrl = Configuration.pdlClientConfig().baseUrl,
            getSkjermingToken = tokenProviderSkjerming::getToken,
        )
    }
    private val tokenProviderSkjerming: AzureTokenProvider by lazy { AzureTokenProvider(config = Configuration.oauthConfigSkjerming()) }
    private val tokenProviderTilgang: AzureTokenProvider by lazy { AzureTokenProvider(config = Configuration.oauthConfigPoaoTilgang()) }
    private val getPoaoTilgangToken: suspend () -> AccessToken = { tokenProviderTilgang.getToken() }

    val poaoTilgangGateway: PoaoTilgangGateway by lazy {
        PoaoTilgangClient(
            baseUrl = Configuration.tilgangClientConfig().baseUrl,
            getToken = { getPoaoTilgangToken.toString() },
        )
    }
    open val personRepo: PersonRepo by lazy {
        PersonPostgresRepo(
            sessionFactory = sessionFactory as PostgresSessionFactory,
        )
    }
    val personService by lazy {
        PersonService(
            personRepo = personRepo,
            personClient = personGateway,
            tilgangsstyringService = tilgangsstyringService,
        )
    }
    val auditService by lazy {
        AuditService(
            personService,
        )
    }
}
