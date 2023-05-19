package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository

object InnsendingRepositoryBuilder {
    internal fun build(): PostgresInnsendingRepository {
        return PostgresInnsendingRepository()
    }
}
