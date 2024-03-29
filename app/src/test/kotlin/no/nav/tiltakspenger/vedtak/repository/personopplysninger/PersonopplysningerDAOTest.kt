package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.innsending.domene.Innsending
import no.nav.tiltakspenger.objectmothers.ObjectMother.barn
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningMaxFyr
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.innsending.PostgresInnsendingRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Random

@Testcontainers
internal class PersonopplysningerDAOTest {

    companion object {
        @Container
        val testcontainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayMigrate()
    }

    private val dao = PersonopplysningerDAO()
    private val repository = PostgresInnsendingRepository()

    @Test
    fun `lagre og hent`() {
        // given
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val fom = 1.januar(2022)
        val tom = 31.mars(2022)
        val innsending = Innsending(journalpostId = journalpostId, ident = ident, fom = fom, tom = tom)
        repository.lagre(innsending)
        val personopplysninger = listOf(personopplysningMaxFyr(ident))

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(innsending.id, personopplysninger, txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(innsending.id, txSession)
            }
        }

        hentet shouldContainExactly personopplysninger
    }

    @Test
    fun `lagre og hent med null-verdier`() {
        // given
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val fom = 1.januar(2022)
        val tom = 31.mars(2022)
        val innsending = Innsending(journalpostId = journalpostId, ident = ident, fom = fom, tom = tom)
        repository.lagre(innsending)
        val personopplysninger = listOf(personopplysningKjedeligFyr(ident = ident))

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(innsending.id, personopplysninger, txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(innsending.id, txSession)
            }
        }

        // then
        hentet shouldContainExactly personopplysninger
    }

    @Test
    fun `hent en som ikke finnes skal gi null tilbake`() {
        // when
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(InnsendingId.random(), txSession)
            }
        }

        // then
        hentet shouldBe emptyList()
    }

    @Test
    fun `legg til personopplysninger for en ident som finnes fra før - da skal de nye dataene gjelde`() {
        // given
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val fom = 1.januar(2022)
        val tom = 31.mars(2022)
        val innsending = Innsending(journalpostId = journalpostId, ident = ident, fom = fom, tom = tom)
        repository.lagre(innsending)
        val gamlePersonopplysninger = personopplysningKjedeligFyr(ident, strengtFortroligUtland = false)

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(innsending.id, listOf(gamlePersonopplysninger), txSession)
            }
        }

        val nyePersonopplysninger = listOf(gamlePersonopplysninger.copy(fornavn = "Ole"))

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(innsending.id, nyePersonopplysninger, txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(innsending.id, txSession)
            }
        }

        // then
        hentet shouldContainExactly nyePersonopplysninger
    }

    @Test
    fun `lagre barn og hent opp igjen`() {
        val journalpostId = Random().nextInt().toString()
        val ident = Random().nextInt().toString()
        val fom = 1.januar(2022)
        val tom = 31.mars(2022)
        val innsending = Innsending(journalpostId = journalpostId, ident = ident, fom = fom, tom = tom)
        val barn1 = barn()
        val barn2 = barn()
        val personopplysninger = personopplysningKjedeligFyr(ident = ident)

        repository.lagre(innsending)

        val personopplysningListe = listOf(personopplysninger, barn1, barn2)
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(innsending.id, personopplysningListe, txSession)
            }
        }

        val hentetPersonopplysninger = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(innsending.id, txSession)
            }
        }

        hentetPersonopplysninger shouldContainExactly personopplysningListe
    }
}
