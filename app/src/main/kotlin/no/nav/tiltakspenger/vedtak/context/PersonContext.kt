package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonopplysningerRepo
import no.nav.tiltakspenger.saksbehandling.service.personopplysning.PersonopplysningService
import no.nav.tiltakspenger.saksbehandling.service.personopplysning.PersonopplysningServiceImpl
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auditlog.AuditService
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.person.PersonHttpklient
import no.nav.tiltakspenger.vedtak.clients.person.PersonService
import no.nav.tiltakspenger.vedtak.repository.person.PersonRepo
import no.nav.tiltakspenger.vedtak.repository.person.PersonRepoImpl
import no.nav.tiltakspenger.vedtak.repository.sak.PersonopplysningerPostgresRepo

@Suppress("unused")
open class PersonContext(
    sessionFactory: SessionFactory,
) {
    val tokenProviderPdl by lazy { AzureTokenProvider(config = Configuration.ouathConfigPdl()) }
    val personopplysningServiceImpl: PersonopplysningService by lazy {
        PersonopplysningServiceImpl(
            personopplysningerRepo,
        )
    }

    open val personGateway: PersonGateway by lazy {
        PersonHttpklient(
            endepunkt = Configuration.pdlClientConfig().baseUrl,
            azureTokenProvider = tokenProviderPdl,
        )
    }
    open val personopplysningerRepo: PersonopplysningerRepo by lazy {
        PersonopplysningerPostgresRepo(
            sessionFactory as PostgresSessionFactory,
        )
    }
    open val personRepo: PersonRepo by lazy {
        PersonRepoImpl(
            sessionFactory = sessionFactory as PostgresSessionFactory,
        )
    }
    val personService by lazy {
        PersonService(
            personRepo = personRepo,
            personClient = personGateway,
        )
    }
    val auditService by lazy {
        AuditService(
            personService,
        )
    }
}
