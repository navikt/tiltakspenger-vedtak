package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningFødselsdato
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.Tiltak
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import org.junit.jupiter.api.Test
import java.util.Random

internal class TiltakDAOTest {

    companion object {
        val random = Random()
    }

    @Test
    fun `lagre og hente med null felter`() {
        val tiltak = tiltak(null)

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val tiltakDAO = testDataHelper.tiltakDAO
            val behandlingRepo = testDataHelper.behandlingRepo
            val sakRepo = testDataHelper.sakRepo

            val behandling = lagreSakOgBehandling(
                saksnummer = Saksnummer("202301011003"),
                behandlingRepo = behandlingRepo,
                sakRepo = sakRepo,
            )

            testDataHelper.sessionFactory.withTransaction { txSession ->
                tiltakDAO.lagre(behandling.id, listOf(tiltak), txSession)
            }

            testDataHelper.sessionFactory.withTransaction { txSession ->
                val hentet = tiltakDAO.hent(behandling.id, txSession)
                hentet.size shouldBe 1
                hentet.first() shouldBe tiltak
            }
        }
    }

    @Test
    fun `lagre og hente med non-null felter`() {
        val tiltak = tiltak(100.0F)

        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val tiltakDAO = testDataHelper.tiltakDAO
            val behandlingRepo = testDataHelper.behandlingRepo
            val sakRepo = testDataHelper.sakRepo

            val behandling = lagreSakOgBehandling(
                saksnummer = Saksnummer("202301011003"),
                behandlingRepo = behandlingRepo,
                sakRepo = sakRepo,
            )

            testDataHelper.sessionFactory.withTransaction { txSession ->
                tiltakDAO.lagre(behandling.id, listOf(tiltak), txSession)
            }

            testDataHelper.sessionFactory.withTransaction { txSession ->
                val hentet = tiltakDAO.hent(behandling.id, txSession)
                hentet.size shouldBe 1
                hentet.first() shouldBe tiltak
            }
        }
    }

    private fun lagreSakOgBehandling(
        saksnummer: Saksnummer = Saksnummer("202301011001"),
        behandlingRepo: PostgresBehandlingRepo,
        sakRepo: PostgresSakRepo,
    ): Behandling {
        val journalpostId = random.nextInt().toString()
        val ident = random.nextInt().toString()
        val deltakelseFom = 1.januar(2023)
        val deltakelseTom = 31.mars(2023)
        val sakId = SakId.random()
        val sak = Sak(
            id = sakId,
            ident = ident,
            saknummer = saksnummer,
            periode = Periode(fraOgMed = deltakelseFom, tilOgMed = deltakelseTom),
            behandlinger = listOf(),
            personopplysninger = SakPersonopplysninger(),
            vedtak = listOf(),
        )
        sakRepo.lagre(sak)

        val søknad = ObjectMother.nySøknad(
            journalpostId = journalpostId,
            personopplysninger = ObjectMother.personSøknad(
                ident = ident,
            ),
            tiltak = ObjectMother.søknadTiltak(
                deltakelseFom = deltakelseFom,
                deltakelseTom = deltakelseTom,
            ),
            barnetillegg = listOf(ObjectMother.barnetilleggMedIdent()),
        )

        val behandling = Førstegangsbehandling.opprettBehandling(sakId = sakId, søknad = søknad, fødselsdato = personopplysningFødselsdato())

        return behandlingRepo.lagre(behandling)
    }

    private fun tiltak(prosent: Float?) = Tiltak(
        id = TiltakId.random(),
        eksternId = "1234",
        gjennomføring = Tiltak.Gjennomføring(
            id = "",
            arrangørnavn = "arrangør",
            typeNavn = "Jobbkurs",
            typeKode = "JOBBK",
            rettPåTiltakspenger = true,
        ),
        deltakelseFom = 1.januar(2023),
        deltakelseTom = 31.januar(2023),
        deltakelseStatus = Tiltak.DeltakerStatus(status = "DELTAR", rettTilÅSøke = true),
        deltakelseProsent = prosent,
        kilde = "Komet",
        registrertDato = 1.januarDateTime(2023),
        innhentet = 1.januarDateTime(2023),
        antallDagerSaksopplysninger = AntallDagerSaksopplysninger.initAntallDagerSaksopplysning(
            antallDager = listOf(
                PeriodeMedVerdi(
                    verdi = AntallDager(antallDager = 1, kilde = Kilde.ARENA, saksbehandlerIdent = null),
                    periode =
                    Periode(
                        fraOgMed = 1.januar(2023),
                        tilOgMed = 31.januar(2023),
                    ),
                ),
            ),
            avklarteAntallDager = emptyList(),
        ),
    )
}
