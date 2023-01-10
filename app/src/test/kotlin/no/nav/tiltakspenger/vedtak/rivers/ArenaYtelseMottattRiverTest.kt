package no.nav.tiltakspenger.vedtak.rivers

/*
internal class ArenaYtelseMottattRiverTest {

    private companion object {
        const val IDENT = "04927799109"
        const val JOURNALPOSTID = "foobar"
    }

    private val innsendingRepository = mockk<InnsendingRepository>(relaxed = true)
    private val testRapid = TestRapid()

    init {
        ArenaYtelserMottattRiver(
            rapidsConnection = testRapid,
            innsendingMediator = InnsendingMediator(
                innsendingRepository = innsendingRepository,
                rapidsConnection = testRapid
            )
        )
    }

    @AfterEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `Når ArenaYtelserMottattHendelse oppstår, skal gamle tiltak filtreres bort fra innsending`() {
        val søknadMottatthendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            søknad = nySøknadMedArenaTiltak(journalpostId = JOURNALPOSTID, ident = IDENT)
        )

        val personopplysningerMottatthendelse = nyPersonopplysningHendelse(journalpostId = JOURNALPOSTID)

        val skjermingMottattHendelse = SkjermingMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            ident = IDENT,
            skjerming = Skjerming(ident = IDENT, skjerming = false, innhentet = LocalDateTime.now())
        )

        val tiltakMottattHendelse = ArenaTiltakMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            tiltaksaktivitet = listOf(
                Tiltaksaktivitet(
                    tiltak = Tiltaksaktivitet.Tiltak.ARBRRHDAG,
                    aktivitetId = "",
                    tiltakLokaltNavn = null,
                    arrangør = null,
                    bedriftsnummer = null,
                    deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(null, null),
                    deltakelseProsent = null,
                    deltakerStatus = Tiltaksaktivitet.DeltakerStatus.GJENN,
                    statusSistEndret = null,
                    begrunnelseInnsøking = "",
                    antallDagerPerUke = null,
                    tidsstempelHosOss = LocalDateTime.now()
                )
            )
        )
        val innsending = Innsending(journalpostId = JOURNALPOSTID, ident = IDENT)
        innsending.håndter(søknadMottatthendelse)
        innsending.håndter(personopplysningerMottatthendelse)
        innsending.håndter(skjermingMottattHendelse)
        innsending.håndter(tiltakMottattHendelse)

        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsending
        val arenaYtelserMottattHendelse =
            javaClass.getResource("/arenaYtelserMottattHendelse.json")?.readText(Charsets.UTF_8)!!
        testRapid.sendTestMessage(arenaYtelserMottattHendelse)
        assertEquals(5, innsending.ytelser.size)
    }

    @Test
    fun `Når ArenaYtelser får en løsning på tiltak, skal den ikke sende noen nye behov`() {
        val søknadMottatthendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            søknad = nySøknadMedArenaTiltak(
                journalpostId = JOURNALPOSTID,
                ident = IDENT,
            )
        )

        val personopplysningerMottatthendelse = nyPersonopplysningHendelse(journalpostId = JOURNALPOSTID)

        val skjermingMottattHendelse = SkjermingMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            ident = IDENT,
            skjerming = Skjerming(
                ident = IDENT,
                skjerming = false,
                innhentet = LocalDateTime.now()
            )
        )

        val tiltakMottattHendelse = ArenaTiltakMottattHendelse(
            aktivitetslogg = Aktivitetslogg(forelder = null),
            journalpostId = JOURNALPOSTID,
            tiltaksaktivitet = listOf(
                Tiltaksaktivitet(
                    tiltak = Tiltaksaktivitet.Tiltak.ARBRRHDAG,
                    aktivitetId = "",
                    tiltakLokaltNavn = null,
                    arrangør = null,
                    bedriftsnummer = null,
                    deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(null, null),
                    deltakelseProsent = null,
                    deltakerStatus = Tiltaksaktivitet.DeltakerStatus.GJENN,
                    statusSistEndret = null,
                    begrunnelseInnsøking = "",
                    antallDagerPerUke = null,
                    tidsstempelHosOss = LocalDateTime.now()
                )
            )
        )
        val innsending = Innsending(journalpostId = JOURNALPOSTID, ident = IDENT)
        innsending.håndter(søknadMottatthendelse)
        innsending.håndter(personopplysningerMottatthendelse)
        innsending.håndter(skjermingMottattHendelse)
        innsending.håndter(tiltakMottattHendelse)

        every { innsendingRepository.hent(JOURNALPOSTID) } returns innsending
        testRapid.sendTestMessage(ytelserMottattEvent())
        with(testRapid.inspektør) {
            assertEquals(0, size)
        }
    }

    private fun ytelserMottattEvent(): String =
        """
           {
             "@behov": [
               "arenaytelser"
             ],
             "@id": "test",
             "@behovId": "behovId",
             "journalpostId": "$JOURNALPOSTID",
             "ident": "$IDENT",
             "fom": "2019-10-01",
             "tom": "2022-06-01",
             "testmelding": true,
             "@opprettet": "2022-08-22T15:56:35.310409409",
             "system_read_count": 0,
             "system_participating_services": [
               {
                 "id": "test",
                 "time": "2022-08-22T15:56:35.310409409",
                 "service": "tiltakspenger-arena",
                 "instance": "tiltakspenger-arena-75c76566d6-hpsh9",
                 "image": "ghcr.io/navikt/tiltakspenger-arena:63854e822f84b676f721c95a37dccc2105b0ef57"
               }
             ],
             "@løsning": {
               "arenaytelser": []
             }
           }
        """.trimIndent()
}


 */
