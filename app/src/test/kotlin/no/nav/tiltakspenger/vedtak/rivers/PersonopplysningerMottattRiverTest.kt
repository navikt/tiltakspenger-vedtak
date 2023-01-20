package no.nav.tiltakspenger.vedtak.rivers

/*
internal class PersonopplysningerMottattRiverTest {
    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val søkerRepository = mockk<SøkerRepository>(relaxed = true)
    private val testRapid = TestRapid()
    private val innsendingMediatorSpy =
        spyk(InnsendingMediator(innsendingRepository = innsendingRepository, rapidsConnection = testRapid))
    private val søkerMediatorSpy =
        spyk(SøkerMediator(søkerRepository = søkerRepository, rapidsConnection = testRapid))

    init {
        PersonopplysningerMottattRiver(
            rapidsConnection = testRapid,
            innsendingMediator = innsendingMediatorSpy,
            søkerMediator = søkerMediatorSpy,
        )
    }

    
    @Test
    fun `Når PersonopplysningerRiver får en løsning på person, skal den sende en behovsmelding etter skjerming`() {
        val journalpostId = "foobar3"
        val ident = "04927799109"
        val personopplysningerMottattHendelse =
            File("src/test/resources/personopplysningerMottattHendelse.json").readText()
        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = journalpostId,
            søknad = nySøknadMedArenaTiltak(
                ident = ident,
                journalpostId = journalpostId,
            )
        )
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
        every { innsendingRepository.hent(journalpostId) } returns innsending

        innsending.håndter(søknadMottattHendelse)
        testRapid.sendTestMessage(personopplysningerMottattHendelse)

        assertEquals(InnsendingTilstandType.AvventerSkjermingdata, innsending.tilstand.type)
        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals("behov", field(0, "@event_name").asText())
            assertEquals("skjerming", field(0, "@behov")[0].asText())
            assertEquals(InnsendingTilstandType.AvventerPersonopplysninger.name, field(0, "tilstandtype").asText())
            assertEquals(journalpostId, field(0, "journalpostId").asText())
            verify {
                innsendingMediatorSpy.håndter(
                    withArg<PersonopplysningerMottattHendelse> {
                        assertEquals(journalpostId, it.journalpostId())
                        val søkerMedPersonoppl =
                            it.personopplysninger().filterIsInstance<Personopplysninger.Søker>().first()
                        assertEquals(ident, søkerMedPersonoppl.ident)
                        assertEquals(LocalDate.of(1983, Month.JULY, 4), søkerMedPersonoppl.fødselsdato)
                        assertEquals("Knuslete", søkerMedPersonoppl.fornavn)
                        assertEquals("Melon", søkerMedPersonoppl.mellomnavn)
                        assertEquals("Ekspedisjon", søkerMedPersonoppl.etternavn)
                        assertEquals("Oslo", søkerMedPersonoppl.kommune)
                        assertEquals("460105", søkerMedPersonoppl.bydel)
                        assertFalse(søkerMedPersonoppl.fortrolig)
                        assertFalse(søkerMedPersonoppl.strengtFortrolig)
                        assertNull(søkerMedPersonoppl.skjermet)
                        val barnOpplysninger =
                            it.personopplysninger().filterIsInstance<Personopplysninger.BarnMedIdent>()
                        assertEquals(1, barnOpplysninger.size)
                        assertEquals("Fornem", barnOpplysninger.first().fornavn)
                        assertEquals("Jogger", barnOpplysninger.first().etternavn)
                        assertEquals("07085512345", barnOpplysninger.first().ident)
                        val barnUtenIden = it.personopplysninger().filterIsInstance<Personopplysninger.BarnUtenIdent>()
                        assertEquals(1, barnUtenIden.size)
                        assertEquals("Liten", barnUtenIden.first().fornavn)
                        assertEquals("Opal", barnUtenIden.first().etternavn)
                        assertEquals(LocalDate.of(2052, Month.JANUARY, 1), barnUtenIden.first().fødselsdato)
                    }
                )
            }
        }
    }
}


 */
