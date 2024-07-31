package no.nav.tiltakspenger.saksbehandling.service

import io.mockk.every
import io.mockk.mockk
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.ports.SøkerRepository
import no.nav.tiltakspenger.saksbehandling.service.søker.SøkerServiceImpl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SøkerServiceTest {

    private val søkerRepo = mockk<SøkerRepository>()
    private val service = SøkerServiceImpl(søkerRepo)

    @Test
    fun `skal kunne hente behandlingDTO`() {
        val fnr = Fnr.random()

        val søker = Søker.fromDb(
            søkerId = SøkerId.random(),
            fnr = fnr,
            personopplysninger = personopplysningKjedeligFyr(fnr = fnr),
        )
        every { søkerRepo.hent(søker.søkerId) } returns søker
        every { søkerRepo.findByIdent(any()) } returns søker

        val søkerDTO = service.hentSøkerIdOrNull(fnr, saksbehandler())
        assertNotNull(søkerDTO)
    }

    @Test
    fun `skal ikke ha tilgang`() {
        val ident = Fnr.random()

        val søker = Søker.fromDb(
            søkerId = SøkerId.random(),
            fnr = ident,
            personopplysninger = personopplysningKjedeligFyr(
                fnr = ident,
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
