package no.nav.tiltakspenger.vedtak.repository.personopplysninger

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotliquery.sessionOf

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.objectmothers.barn
import no.nav.tiltakspenger.objectmothers.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.personopplysningMaxFyr
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vedtak.repository.søker.PostgresSøkerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

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
    private val søkerRepository = PostgresSøkerRepository()

    @Test
    fun `lagre og hent`() {
        // given
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val personopplysninger = listOf(personopplysningMaxFyr(ident))

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, personopplysninger, txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søker.id, txSession)
            }
        }

        hentet shouldContainExactly personopplysninger
    }

    @Test
    fun `lagre og hent med null-verdier`() {
        // given
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val personopplysninger = listOf(personopplysningKjedeligFyr(ident = ident))

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, personopplysninger, txSession)
            }
        }
        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søker.id, txSession)
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
                dao.hent(SøkerId.random(), txSession)
            }
        }

        // then
        hentet shouldBe emptyList()
    }

    @Test
    fun `legg til personopplysninger for en ident som finnes fra før - da skal de nye dataene gjelde`() {
        // given
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        søkerRepository.lagre(søker)
        val gamlePersonopplysninger = personopplysningKjedeligFyr(ident)

        // when
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, listOf(gamlePersonopplysninger), txSession)
            }
        }

        val nyePersonopplysninger = listOf(gamlePersonopplysninger.copy(fornavn = "Ole"))

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, nyePersonopplysninger, txSession)
            }
        }

        val hentet = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søker.id, txSession)
            }
        }

        // then
        hentet shouldContainExactly nyePersonopplysninger
    }

    @Test
    fun `lagre barn og hent opp igjen`() {
        val ident = Random().nextInt().toString()
        val søker = Søker(ident)
        val barn1 = barn()
        val barn2 = barn()
        val personopplysninger = personopplysningKjedeligFyr(ident = ident)

        søkerRepository.lagre(søker)

        val personopplysningListe = listOf(personopplysninger, barn1, barn2)
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.lagre(søker.id, personopplysningListe, txSession)
            }
        }

        val hentetPersonopplysninger = sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                dao.hent(søker.id, txSession)
            }
        }

        hentetPersonopplysninger shouldContainExactly personopplysningListe
    }
}
