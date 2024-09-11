package no.nav.tiltakspenger.saksbehandling.service

import arrow.core.left
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.common.getOrFail
import no.nav.tiltakspenger.libs.common.Rolle.SAKSBEHANDLER
import no.nav.tiltakspenger.libs.common.Rolle.SKJERMING
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.nySøknad
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling
import org.junit.jupiter.api.Test

internal class SakServiceTest {
    @Test
    fun `sjekk at skjerming blir satt riktig`() {
        with(TestApplicationContext()) {
            val saksbehandler = ObjectMother.saksbehandler(roller = Roller(listOf(SAKSBEHANDLER, SKJERMING)))
            val søknad = this.nySøknad(erSkjermet = true)

            this.sakContext.sakService.startFørstegangsbehandling(
                søknad.id,
                ObjectMother.saksbehandler(),
            ) shouldBe KanIkkeStarteFørstegangsbehandling.HarIkkeTilgangTilPerson.left()

            val sak =
                this.sakContext.sakService
                    .startFørstegangsbehandling(søknad.id, saksbehandler)
                    .getOrFail()

            sak.personopplysninger.søker().skjermet shouldBe true
        }
    }
}
