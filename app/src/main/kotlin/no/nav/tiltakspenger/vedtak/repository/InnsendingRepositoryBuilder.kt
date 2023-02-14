package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.BarnetilleggDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.TiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.TrygdOgPensjonDAO

object InnsendingRepositoryBuilder {
    internal fun build(): PostgresInnsendingRepository {
        return PostgresInnsendingRepository(
            søknadDAO = SøknadDAO(
                barnetilleggDAO = BarnetilleggDAO(),
                tiltakDAO = TiltakDAO(),
                trygdOgPensjonDAO = TrygdOgPensjonDAO(),
            ),
        )
    }
}
