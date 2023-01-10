package no.nav.tiltakspenger.vedtak.rivers

/*
internal class SkjermingMottattRiverTest {

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val journalpostId = "wolla"
    private val ident = "05906398291"
    private val løsning = """
            {
              "@behov": [
                "skjerming"
              ],
              "@id": "test",
              "@behovId": "behovId",
              "journalpostId": "wolla",
              "ident": "05906398291",
              "fom": "2019-10-01",
              "tom": "2022-06-01",
              "@opprettet": "2022-08-19T12:28:01.422516717",
              "system_read_count": 0,
              "system_participating_services": [
                {
                  "id": "test",
                  "time": "2022-08-19T12:28:01.422516717",
                  "service": "tiltakspenger-skjerming",
                  "instance": "tiltakspenger-skjerming-69f669bc95-plxb6",
                  "image": "ghcr.io/navikt/tiltakspenger-skjerming:128cdcc92ea50224bbccdc4c565e3f408e093213"
                }
              ],
              "@løsning": {
                "skjerming": false
              }
            }
        """

    init {
        SkjermingMottattRiver(
            rapidsConnection = testRapid,
            innsendingMediator = InnsendingMediator(
                innsendingRepository = innsendingRepository,
                rapidsConnection = testRapid,
            ),
            søkerMediator = SøkerMediator(
                søkerRepository = søkerRepository,
                rapidsConnection = testRapid,
            )
        )
    }

    @Test
    fun `En løsning for skjerming mottas`() {
        // given
        val aktivitetslogg = Aktivitetslogg(forelder = null)
        val mottattSøknadHendelse = SøknadMottattHendelse(
            aktivitetslogg = aktivitetslogg,
            journalpostId = journalpostId,
            søknad = nySøknadMedArenaTiltak(
                journalpostId = journalpostId,
                ident = ident,
            )
        )
        val personopplysningerMottattHendelse = nyPersonopplysningHendelse(journalpostId = journalpostId)
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
        every { innsendingRepository.hent(journalpostId) } returns innsending

        // when
        innsending.håndter(mottattSøknadHendelse)
        innsending.håndter(personopplysningerMottattHendelse)
        testRapid.sendTestMessage(løsning)

        // then
        with(testRapid.inspektør) {
            assertEquals(1, this.size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals(ident, field(0, "ident").asText())
            assertEquals(journalpostId, field(0, "journalpostId").asText())
            assertEquals("arenatiltak", field(0, "@behov")[0].asText())
        }
    }
}


 */
