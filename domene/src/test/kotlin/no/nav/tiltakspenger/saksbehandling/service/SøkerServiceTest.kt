package no.nav.tiltakspenger.saksbehandling.service

import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerServiceImpl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Random

internal class SøkerServiceTest {

    private val søkerRepo = mockk<SøkerRepository>()
    private val service = SøkerServiceImpl(søkerRepo)
    private val random = Random()

    @Test
    fun `skal kunne hente behandlingDTO`() {
        val ident = random.nextInt().toString()

        val søker = Søker.fromDb(
            søkerId = SøkerId.random(),
            ident = ident,
            personopplysninger = personopplysningKjedeligFyr(ident = ident),
        )
        every { søkerRepo.hent(søker.søkerId) } returns søker
        every { søkerRepo.findByIdent(any()) } returns søker

        val søkerDTO = service.hentSøkerIdOrNull(ident, saksbehandler())
        assertNotNull(søkerDTO)
    }

    @Test
    fun `skal ikke ha tilgang`() {
        val ident = random.nextInt().toString()

        val søker = Søker.fromDb(
            søkerId = SøkerId.random(),
            ident = ident,
            personopplysninger = personopplysningKjedeligFyr(
                ident = ident,
                strengtFortrolig = true,
            ),
        )

        every { søkerRepo.hent(søker.søkerId) } returns søker
        every { søkerRepo.findByIdent(any()) } returns søker

        assertThrows<TilgangException> {
            service.hentSøkerIdOrNull(ident, saksbehandler())
        }
    }
}
