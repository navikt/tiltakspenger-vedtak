package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.sessionOf
import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.*

@Testcontainers
internal class TrygdOgPensjonDAOTest {

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre trygdogpensjon og hente de ut igjen (må dessverre lagre både søker og søknad pga foreign keys)`() {
        val søknadDAO = PostgresSøknadDAO()
        val søkerRepository = PostgresSøkerRepository(søknadDAO)
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val søknadId = UUID.randomUUID()
        val søknad = enSøknad(søknadId, ident)
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(søker.id, listOf(søknad), txSession)
            }
        }
        val trygdOgPensjonMedNull = TrygdOgPensjon(
            utbetaler = "utbetaler",
            prosent = null,
            fom = null,
            tom = null,
        )
        val trygdOgPensjonUtenNull = TrygdOgPensjon(
            utbetaler = "utbetaler",
            prosent = 50,
            fom = 1.januar(2022),
            tom = 31.januar(2022),
        )

        val trygdOgPensjonDAO = TrygdOgPensjonDAO()
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                trygdOgPensjonDAO.lagre(
                    søknadId = søknadId,
                    trygdOgPensjon = listOf(trygdOgPensjonMedNull, trygdOgPensjonUtenNull),
                    txSession
                )
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                trygdOgPensjonDAO.hentTrygdOgPensjonListe(søknadId = søknadId, txSession = txSession)
            }
        }

        assertEquals(2, hentet.size)
        assertTrue(hentet.contains(trygdOgPensjonMedNull))
        assertTrue(hentet.contains(trygdOgPensjonUtenNull))
    }

    private fun enSøknad(id: UUID, ident: String) = Søknad(
        id = id,
        søknadId = "41",
        journalpostId = "42",
        dokumentInfoId = "43",
        fornavn = null,
        etternavn = null,
        ident = ident,
        deltarKvp = false,
        deltarIntroduksjonsprogrammet = null,
        oppholdInstitusjon = null,
        typeInstitusjon = null,
        opprettet = null,
        barnetillegg = emptyList(),
        tidsstempelHosOss = LocalDateTime.now(),
        tiltak = Tiltak.ArenaTiltak(
            arenaId = "123",
            arrangoernavn = "Fest og morro",
            harSluttdatoFraArena = true,
            tiltakskode = Tiltaksaktivitet.Tiltak.GRUPPEAMO,
            erIEndreStatus = false,
            opprinneligSluttdato = LocalDate.now(),
            opprinneligStartdato = LocalDate.now(),
            sluttdato = LocalDate.now(),
            startdato = LocalDate.now()
        ),
        trygdOgPensjon = emptyList(),
        fritekst = null,
    )
}
