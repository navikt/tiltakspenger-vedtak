package no.nav.tiltakspenger.vedtak.repository.søknad

import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.juni
import no.nav.tiltakspenger.objectmothers.ObjectMother.fraOgMedDatoNei
import no.nav.tiltakspenger.objectmothers.ObjectMother.nei
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeNei
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
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
import java.time.Month
import java.util.Random

@Testcontainers
internal class BarnetilleggDAOTest {

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    @Test
    fun `lagre barnetillegg og hente de ut igjen (må dessverre lagre både innsending og søknad pga foreign keys)`() {
        val søknadDAO = SøknadDAO()
        val repository = PostgresInnsendingRepository(søknadDAO)
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val innsending = Innsending(journalpostId = journalpostId, ident = ident)
        repository.lagre(innsending)
        val søknadId = Søknad.randomId()
        val søknad = enSøknad(søknadId, ident)
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                søknadDAO.lagre(innsending.id, søknad, txSession)
            }
        }
        val barnetilleggFraPdl =
            Barnetillegg.FraPdl(
                oppholderSegIEØS = Søknad.JaNeiSpm.Ja,
                fornavn = "fornavn",
                mellomnavn = "mellomnavn",
                etternavn = "etternavn",
                fødselsdato = 14.juni(2012),
            )
        val barnetilleggManuell =
            Barnetillegg.Manuell(
                oppholderSegIEØS = Søknad.JaNeiSpm.Ja,
                fornavn = "fornavn",
                mellomnavn = null,
                etternavn = "etternavn",
                fødselsdato = LocalDate.of(2022, Month.AUGUST, 19),
            )

        val barnetilleggDAO = BarnetilleggDAO()
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                barnetilleggDAO.lagre(
                    søknadId = søknadId,
                    barnetillegg = listOf(barnetilleggFraPdl, barnetilleggManuell),
                    txSession,
                )
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                barnetilleggDAO.hentBarnetilleggListe(søknadId = søknadId, txSession = txSession)
            }
        }

        assertEquals(2, hentet.size)
        assertTrue(hentet.contains(barnetilleggFraPdl))
        assertTrue(hentet.contains(barnetilleggManuell))
    }

    private fun enSøknad(id: SøknadId, ident: String) = Søknad(
        id = id,
        søknadId = "41",
        journalpostId = "42",
        dokumentInfoId = "43",
        filnavn = "filnavn",
        personopplysninger = Søknad.Personopplysninger(
            fornavn = "fornavn",
            etternavn = "etternavn",
            ident = ident,
        ),
        tiltak = Tiltak.ArenaTiltak(
            arenaId = "123",
            arrangoernavn = "Fest og morro",
            tiltakskode = Tiltaksaktivitet.Tiltak.GRUPPEAMO,
            opprinneligSluttdato = LocalDate.now(),
            opprinneligStartdato = LocalDate.now(),
            sluttdato = LocalDate.now(),
            startdato = LocalDate.now(),
        ),
        barnetillegg = emptyList(),
        opprettet = LocalDateTime.now(),
        tidsstempelHosOss = LocalDateTime.now(),
        vedlegg = emptyList(),
        kvp = periodeNei(),
        intro = periodeNei(),
        institusjon = periodeNei(),
        etterlønn = nei(),
        gjenlevendepensjon = periodeNei(),
        alderspensjon = fraOgMedDatoNei(),
        sykepenger = periodeNei(),
        supplerendeStønadAlder = periodeNei(),
        supplerendeStønadFlyktning = periodeNei(),
        jobbsjansen = periodeNei(),
        trygdOgPensjon = nei(),
    )
}
