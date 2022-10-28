package no.nav.tiltakspenger.vedtak.service

import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.objectmothers.barnetilleggMedIdent
import no.nav.tiltakspenger.objectmothers.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.saksbehandler
import no.nav.tiltakspenger.objectmothers.søkerMedPersonopplysninger
import no.nav.tiltakspenger.objectmothers.søkerMedSøknad
import no.nav.tiltakspenger.objectmothers.trygdOgPensjon
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import no.nav.tiltakspenger.vedtak.service.søknad.SøknadServiceImpl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class SøknadServiceTest {

    private val repo = mockk<SøkerRepository>()
    private val service = SøknadServiceImpl(repo)

    @Test
    fun `skal kunne hente behandlingDTO`() {

        val ident = Random().nextInt().toString()
        val søknad = nySøknadMedBrukerTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val søker = søkerMedPersonopplysninger(
            ident = ident,
            søknad = søknad,
        )

        every { repo.findBySøknadId(søknad.søknadId) } returns søker

        val behandlingDTO = service.hentBehandlingAvSøknad(søknad.søknadId, saksbehandler())

        assertNotNull(behandlingDTO)
    }

    @Test
    fun `skal ikke ha tilgang`() {

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

        assertThrows<TilgangException> {
            service.hentBehandlingAvSøknad(søknad.søknadId, saksbehandler())
        }

    }
}
