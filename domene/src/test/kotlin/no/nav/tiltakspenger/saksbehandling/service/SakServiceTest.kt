package no.nav.tiltakspenger.saksbehandling.service

internal class SakServiceTest {
    /*
    @Test
    fun `sjekk at skjerming blir satt riktig`() = runTest {
        with(TestApplicationContext()) {
            val saksbehandler = ObjectMother.saksbehandler(roller = Roller(listOf(SAKSBEHANDLER, SKJERMING)))
            val søknad = this.nySøknad()

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
     */
}
