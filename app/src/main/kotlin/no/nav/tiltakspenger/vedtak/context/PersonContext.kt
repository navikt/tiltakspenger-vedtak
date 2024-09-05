package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.service.personopplysning.PersonopplysningServiceImpl
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.person.PersonHttpklient
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerPostgresRepo

@Suppress("unused")
internal open class PersonContext(
    val personopplysningServiceImpl: PersonopplysningServiceImpl,
    val personGateway: PersonHttpklient,
    val personopplysningRepo: PersonopplysningerPostgresRepo,
    val tokenProviderPdl: AzureTokenProvider,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
        ): PersonContext {
            val personopplysningRepo = PersonopplysningerPostgresRepo(sessionFactory)
            val personopplysningServiceImpl = PersonopplysningServiceImpl(personopplysningRepo)
            val tokenProviderPdl = AzureTokenProvider(config = Configuration.ouathConfigPdl())
            val personGateway = PersonHttpklient(
                endepunkt = Configuration.pdlClientConfig().baseUrl,
                azureTokenProvider = tokenProviderPdl,
            )
            return PersonContext(
                personopplysningServiceImpl = personopplysningServiceImpl,
                personGateway = personGateway,
                personopplysningRepo = personopplysningRepo,
                tokenProviderPdl = tokenProviderPdl,
            )
        }
    }
}
