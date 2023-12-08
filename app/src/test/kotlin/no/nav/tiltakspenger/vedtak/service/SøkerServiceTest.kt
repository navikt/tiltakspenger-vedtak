package no.nav.tiltakspenger.vedtak.service

import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.exceptions.TilgangException
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.objectmothers.ObjectMother.barnetilleggMedIdent
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedPersonopplysninger
import no.nav.tiltakspenger.objectmothers.ObjectMother.innsendingMedSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.vedtak.innsending.Søker
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
    private val service = SøkerServiceImpl(søkerRepo)

    @Test
    fun `skal kunne hente behandlingDTO`() {
        val ident = Random().nextInt().toString()
        val søknad = nySøknad(
            personopplysninger = personSøknad(
                ident = ident,
            ),
            barnetillegg = listOf(barnetilleggMedIdent()),
        )
        val innsending = innsendingMedPersonopplysninger(
            ident = ident,
            søknad = søknad,
        )

        val søker = Søker.fromDb(
            søkerId = SøkerId.random(),
            ident = ident,
            personopplysninger = innsending.personopplysningerSøker(),
        )
        every { innsendingRepo.findByIdent(søker.ident) } returns listOf(innsending)
        every { søkerRepo.hent(søker.søkerId) } returns søker
        every { søkerRepo.findByIdent(any()) } returns søker

        val søkerDTO = service.hentSøkerId(ident, saksbehandler())
        assertNotNull(søkerDTO)
    }

    @Test
    fun `skal ikke ha tilgang`() {
        val ident = Random().nextInt().toString()
        val søknad = nySøknad(
            personopplysninger = personSøknad(
                ident = ident,
            ),
            barnetillegg = listOf(barnetilleggMedIdent()),
        )
        val innsending = innsendingMedSøknad(
            ident = ident,
            søknad = søknad,
        )

        val søker = Søker.fromDb(
            søkerId = SøkerId.random(),
            ident = ident,
            personopplysninger = innsending.personopplysningerSøker(),
        )

        every { innsendingRepo.findByIdent(søker.ident) } returns listOf(innsending)
        every { søkerRepo.hent(søker.søkerId) } returns søker
        every { søkerRepo.findByIdent(any()) } returns søker

        assertThrows<TilgangException> {
            service.hentSøkerId(ident, saksbehandler())
        }
    }
}
