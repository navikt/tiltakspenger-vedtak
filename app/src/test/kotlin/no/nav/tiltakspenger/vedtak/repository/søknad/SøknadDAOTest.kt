package no.nav.tiltakspenger.vedtak.repository.søknad
//
// import kotliquery.sessionOf
// import no.nav.tiltakspenger.objectmothers.ObjectMother.behandling
// import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
// import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeNei
// import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
// import no.nav.tiltakspenger.vedtak.Søknad
// import no.nav.tiltakspenger.vedtak.db.DataSource
// import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
// import no.nav.tiltakspenger.vedtak.db.flywayMigrate
// import no.nav.tiltakspenger.vedtak.repository.behandling.PostgresBehandlingRepo
// import org.junit.jupiter.api.Assertions.assertEquals
// import org.junit.jupiter.api.Assertions.assertNotNull
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.Test
// import org.testcontainers.junit.jupiter.Container
// import org.testcontainers.junit.jupiter.Testcontainers
// import java.time.LocalDateTime
// import java.time.Month
// import java.util.Random
//
// TODO () Denne må fikses. Sjekk VedtakDAOTest for hvordan
// @Testcontainers
// internal class SøknadDAOTest {
//    private val søknadDAO = SøknadDAO()
//    private val repository = PostgresBehandlingRepo()
//
//    companion object {
//        @Container
//        val postgresContainer = PostgresTestcontainer
//    }
//
//    @BeforeEach
//    fun setup() {
//        flywayMigrate()
//    }
//
//    @Test
//    fun `lagre og hente med null-felter`() {
//        val ident = "3"
// //        val journalpostId = Random().nextInt().toString()
// //        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
//        val behandling = behandling()
//        repository.lagre(behandling)
//        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
//        val uuid = Søknad.randomId()
//        val søknad = nySøknad(
//            id = uuid,
//            søknadId = "41",
//            journalpostId = "42",
//            dokumentInfoId = "43",
//            personopplysninger = Søknad.Personopplysninger(
//                fornavn = "fornavn",
//                etternavn = "etternavn",
//                ident = ident,
//            ),
//            kvp = periodeNei(),
//            intro = periodeNei(),
//            institusjon = periodeNei(),
//            opprettet = innhentet,
//            barnetillegg = emptyList(),
//            tidsstempelHosOss = innhentet,
//            tiltak = søknadTiltak(),
//            vedlegg = emptyList(),
//        )
//        sessionOf(DataSource.hikariDataSource).use {
//            it.transaction { txSession ->
//                søknadDAO.lagre(behandling.id, listOf(søknad), txSession)
//            }
//        }
//
//        val hentet: List<Søknad> = sessionOf(DataSource.hikariDataSource).use {
//            it.transaction { txSession ->
//                søknadDAO.hent(behandling.id, txSession)
//            }
//        }
//
//        assertNotNull(hentet)
//        assertEquals(uuid, hentet.first().id)
//        assertEquals(ident, hentet.first().personopplysninger.ident)
//        assertEquals(innhentet, hentet.first().tidsstempelHosOss)
//    }
//
//    // TODO() fikse disse når den over går ok...
// //    @Test
// //    fun `lagre og hente med null-felter og underliggende klasser`() {
// //        val ident = "4"
// //        val journalpostId = Random().nextInt().toString()
// //        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
// //
// //        repository.lagre(innsending)
// //        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
// //        val uuid = Søknad.randomId()
// //        val søknad = nySøknad(
// //            id = uuid,
// //            søknadId = "41",
// //            journalpostId = "42",
// //            dokumentInfoId = "43",
// //            personopplysninger = Søknad.Personopplysninger(
// //                fornavn = "fornavn",
// //                etternavn = "etternavn",
// //                ident = ident,
// //            ),
// //            kvp = periodeNei(),
// //            intro = periodeNei(),
// //            institusjon = periodeNei(),
// //            opprettet = innhentet,
// //            barnetillegg = listOf(
// //                Barnetillegg.FraPdl(
// //                    oppholderSegIEØS = ja(),
// //                    fornavn = "fornavn",
// //                    mellomnavn = "mellomnavn",
// //                    etternavn = "etternavn",
// //                    fødselsdato = 1.januar(2020),
// //                ),
// //            ),
// //            tidsstempelHosOss = innhentet,
// //            tiltak = søknadTiltak(),
// //            vedlegg = listOf(
// //                Vedlegg(
// //                    journalpostId = "journalpostId",
// //                    dokumentInfoId = "dokumentId",
// //                    filnavn = "filnavn",
// //                ),
// //            ),
// //        )
// //        sessionOf(DataSource.hikariDataSource).use {
// //            it.transaction { txSession ->
// //                søknadDAO.lagre(innsending.id, søknad, txSession)
// //            }
// //        }
// //
// //        val hentet: Søknad? = sessionOf(DataSource.hikariDataSource).use {
// //            it.transaction { txSession ->
// //                søknadDAO.hent(innsending.id, txSession)
// //            }
// //        }
// //
// //        assertNotNull(hentet)
// //        assertEquals(uuid, hentet!!.id)
// //        assertEquals(ident, hentet.personopplysninger.ident)
// //        assertEquals(innhentet, hentet.tidsstempelHosOss)
// //
// //        assertNotNull(hentet.tiltak)
// //        assertEquals(1, hentet.barnetillegg.size)
// //        assertEquals(1, hentet.vedlegg.size)
// //        assertTrue(hentet.intro is Søknad.PeriodeSpm.Nei)
// //        assertTrue(hentet.kvp is Søknad.PeriodeSpm.Nei)
// //    }
// //
// //    @Test
// //    fun `lagre og hente med fyllte felter og underliggende klasser`() {
// //        val ident = "5"
// //        val journalpostId = Random().nextInt().toString()
// //        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
// //        repository.lagre(innsending)
// //        val innhentet = LocalDateTime.of(2022, Month.AUGUST, 15, 23, 23)
// //        val uuid = Søknad.randomId()
// //        val tiltak = søknadTiltak()
// //        val søknad = nySøknad(
// //            id = uuid,
// //            søknadId = "41",
// //            journalpostId = "42",
// //            dokumentInfoId = "43",
// //            personopplysninger = Søknad.Personopplysninger(
// //                fornavn = "fornavn",
// //                etternavn = "etternavn",
// //                ident = ident,
// //            ),
// //            kvp = periodeJa(
// //                fom = 15.august(2022),
// //                tom = 30.august(2022),
// //            ),
// //            intro = periodeJa(
// //                fom = 15.august(2022),
// //                tom = 30.august(2022),
// //            ),
// //            institusjon = periodeJa(
// //                fom = 15.august(2022),
// //                tom = 30.august(2022),
// //            ),
// //            opprettet = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
// //            barnetillegg = listOf(
// //                Barnetillegg.FraPdl(
// //                    oppholderSegIEØS = Søknad.JaNeiSpm.Ja,
// //                    fornavn = "foranvn",
// //                    mellomnavn = "mellomnavn",
// //                    etternavn = "etternavn",
// //                    fødselsdato = 15.august(2022),
// //                ),
// //            ),
// //            tidsstempelHosOss = innhentet,
// //            tiltak = tiltak,
// //            vedlegg = listOf(
// //                Vedlegg(
// //                    journalpostId = "journalpostId",
// //                    dokumentInfoId = "dokumentId",
// //                    filnavn = "filnavn",
// //                ),
// //            ),
// //        )
// //        sessionOf(DataSource.hikariDataSource).use {
// //            it.transaction { txSession ->
// //                søknadDAO.lagre(innsending.id, søknad, txSession)
// //            }
// //        }
// //
// //        val hentet = sessionOf(DataSource.hikariDataSource).use {
// //            it.transaction { txSession ->
// //                søknadDAO.hent(innsending.id, txSession)
// //            }
// //        }
// //
// //        assertNotNull(hentet)
// //        assertEquals(uuid, hentet!!.id)
// //        assertEquals(ident, hentet.personopplysninger.ident)
// //        assertEquals(innhentet, hentet.tidsstempelHosOss)
// //
// //        assertNotNull(hentet.tiltak)
// //        assertEquals(1, hentet.barnetillegg.size)
// //
// //        // TODO: Denne må erstattes
// //        /*
// //        hentet::class.declaredMemberProperties.forEach {
// //            assertNotNull(it.call(hentet))
// //        }
// //         */
// //
// //        assertEquals(søknad.intro, hentet.intro)
// //        assertEquals(søknad.kvp, hentet.kvp)
// //
// //        val barnetillegg = hentet.barnetillegg.first()
// //        barnetillegg::class.declaredMemberProperties.forEach {
// //            assertNotNull(it.call(barnetillegg))
// //        }
// //
// //        hentet.tiltak shouldBe tiltak
// //        assertEquals(tiltak, hentet.tiltak)
// //
// //        // Sjekker verdiene for noen litt tilfeldige felter også:
// //        assertEquals(søknad.opprettet, hentet.opprettet)
// //        println(søknad.opprettet)
// //        assertEquals(søknad.opprettet, hentet.opprettet)
// //        assertEquals(søknad.tidsstempelHosOss, hentet.tidsstempelHosOss)
// //
// //        assertEquals(
// //            søknad.tiltak.sluttDato,
// //            hentet.tiltak.sluttDato,
// //        )
// //
// //        assertEquals(søknad.vedlegg.first().journalpostId, hentet.vedlegg.first().journalpostId)
// //        assertEquals(søknad.vedlegg.first().dokumentInfoId, hentet.vedlegg.first().dokumentInfoId)
// //        assertEquals(søknad.vedlegg.first().filnavn, hentet.vedlegg.first().filnavn)
// //    }
// }
