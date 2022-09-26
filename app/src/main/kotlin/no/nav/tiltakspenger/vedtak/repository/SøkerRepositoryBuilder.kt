package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.BarnetilleggDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.TiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.TrygdOgPensjonDAO

object SøkerRepositoryBuilder {
    internal fun build(): PostgresSøkerRepository {
        return PostgresSøkerRepository(
            søknadDAO = SøknadDAO(
                barnetilleggDAO = BarnetilleggDAO(),
                tiltakDAO = TiltakDAO(),
                trygdOgPensjonDAO = TrygdOgPensjonDAO()
            )
        )
    }
}
