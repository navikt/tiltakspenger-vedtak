package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Random

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
        val søknadDAO = SøknadDAO()
        val søkerRepository = PostgresInnsendingRepository(søknadDAO)
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
        søkerRepository.lagre(innsending)
        val søknadId = Søknad.randomId()
        val søknad = enSøknad(søknadId, ident)
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, søknad, txSession)
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
                    txSession,
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

    private fun enSøknad(id: SøknadId, ident: String) = Søknad(
        id = id,
        søknadId = "41",
        journalpostId = "42",
        dokumentInfoId = "43",
        fornavn = null,
        etternavn = null,
        ident = ident,
        deltarKvp = false,
        deltarIntroduksjonsprogrammet = false,
        introduksjonsprogrammetDetaljer = null,
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
            startdato = LocalDate.now(),
        ),
        trygdOgPensjon = emptyList(),
        fritekst = null,
        vedlegg = emptyList(),
    )
}
