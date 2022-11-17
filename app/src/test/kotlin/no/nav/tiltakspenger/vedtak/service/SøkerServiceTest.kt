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
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class SøkerServiceTest {

    private val repo = mockk<SøkerRepository>()
    private val service = SøkerServiceImpl(repo)

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

        every { repo.hentBySøkerId(søker.id) } returns søker

        val behandlingDTO = service.hentSøkerOgSøknader(søker.id, saksbehandler())

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

        every { repo.hentBySøkerId(søker.id) } returns søker

        assertThrows<TilgangException> {
            service.hentSøkerOgSøknader(søker.id, saksbehandler())
        }

    }
}
