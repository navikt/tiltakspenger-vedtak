package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.service.SøknadService
import no.nav.tiltakspenger.saksbehandling.service.SøknadServiceImpl
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadRepo

internal open class SøknadContext(
    val søknadService: SøknadService,
) {
    companion object {
        fun create(
            sessionFactory: PostgresSessionFactory,
        ): SøknadContext {
            val søknadRepo = PostgresSøknadRepo(sessionFactory = sessionFactory)

            val søknadService = SøknadServiceImpl(søknadRepo)
            return SøknadContext(
                søknadService = søknadService,
            )
        }
    }
}
