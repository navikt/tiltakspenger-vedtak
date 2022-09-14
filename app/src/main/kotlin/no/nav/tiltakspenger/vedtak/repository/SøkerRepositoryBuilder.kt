package no.nav.tiltakspenger.vedtak.repository

import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import no.nav.tiltakspenger.vedtak.repository.søknad.ArenatiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.BarnetilleggDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.BrukertiltakDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.PostgresSøknadDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.TrygdOgPensjonDAO

object SøkerRepositoryBuilder {
    internal fun build(): PostgresSøkerRepository {
        return PostgresSøkerRepository(
            søknadDAO = PostgresSøknadDAO(
                barnetilleggDAO = BarnetilleggDAO(),
                arenatiltakDAO = ArenatiltakDAO(),
                brukertiltakDAO = BrukertiltakDAO(),
                trygdOgPensjonDAO = TrygdOgPensjonDAO()
            )
        )
    }
}
