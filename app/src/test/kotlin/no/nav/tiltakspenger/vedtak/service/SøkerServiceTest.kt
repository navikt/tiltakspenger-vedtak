package no.nav.tiltakspenger.vedtak.service

import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.objectmothers.barnetilleggMedIdent
import no.nav.tiltakspenger.objectmothers.innsendingMedPersonopplysninger
import no.nav.tiltakspenger.objectmothers.innsendingMedSøknad
import no.nav.tiltakspenger.objectmothers.nySøknadMedBrukerTiltak
import no.nav.tiltakspenger.objectmothers.saksbehandler
import no.nav.tiltakspenger.objectmothers.trygdOgPensjon
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.service.søker.SøkerServiceImpl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Random

internal class SøkerServiceTest {

    private val innsendingRepo = mockk<InnsendingRepository>()
    private val søkerRepo = mockk<SøkerRepository>()
    private val service = SøkerServiceImpl(søkerRepo, innsendingRepo)

    @Test
    fun `skal kunne hente behandlingDTO`() {

        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        val søknad = nySøknadMedBrukerTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val innsending = innsendingMedPersonopplysninger(
            ident = ident,
            søknad = søknad,
        )

        every { innsendingRepo.findByIdent(søker.ident) } returns listOf(innsending)
        every { søkerRepo.hent(søker.søkerId) } returns søker

        val søkerDTO = service.hentSøkerOgSøknader(søker.søkerId, saksbehandler())
        assertNotNull(søkerDTO)
    }

    @Test
    fun `skal ikke ha tilgang`() {

        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        val søknad = nySøknadMedBrukerTiltak(
            ident = ident,
            barnetillegg = listOf(barnetilleggMedIdent()),
            trygdOgPensjon = listOf(trygdOgPensjon()),
        )
        val innsending = innsendingMedSøknad(
            ident = ident,
            søknad = søknad,
        )

        every { innsendingRepo.findByIdent(søker.ident) } returns listOf(innsending)
        every { søkerRepo.hent(søker.søkerId) } returns søker

        assertThrows<TilgangException> {
            service.hentSøkerOgSøknader(søker.søkerId, saksbehandler())
        }

    }
}
