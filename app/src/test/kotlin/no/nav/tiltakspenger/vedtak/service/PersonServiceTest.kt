package no.nav.tiltakspenger.vedtak.service

import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.objectmothers.barnetilleggMedIdent
import no.nav.tiltakspenger.objectmothers.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.søkerMedSøknad
import no.nav.tiltakspenger.objectmothers.trygdOgPensjon
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.*

internal class PersonServiceTest {

    private val repo = mockk<SøkerRepository>()
    private val service = PersonServiceImpl(repo)

    @Test
    fun `hentBehandlingAvSøknad`() {

        val ident = Random().nextInt().toString()
        val søknad = nySøknadMedBrukerTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val søker = søkerMedSøknad(
            ident = ident,
            søknad = søknad,
        )

        every { repo.findBySøknadId(søknad.søknadId) } returns søker

        val behandlingDTO = service.hentBehandlingAvSøknad(søknad.søknadId)

        assertNotNull(behandlingDTO)
    }
}
