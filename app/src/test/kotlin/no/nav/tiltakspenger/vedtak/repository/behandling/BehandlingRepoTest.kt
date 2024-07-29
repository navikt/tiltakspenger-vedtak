package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningFødselsdato
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test
import java.util.Random

internal class BehandlingRepoTest {

    companion object {
        val random = Random()
    }

    @Test
    fun `lagre og hente en behandling`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val behandlingRepo = testDataHelper.behandlingRepo
            val sakRepo = testDataHelper.sakRepo

            val journalpostId = random.nextInt().toString()
            val ident = random.nextInt().toString()
            val deltakelseFom = 1.januar(2023)
            val deltakelseTom = 31.mars(2023)
            val sakId = SakId.random()
            val saksnummer = Saksnummer("202301011001")
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

            val registrerteTiltak = listOf(
                ObjectMother.tiltak(),
            )

            val behandling = Førstegangsbehandling.opprettBehandling(
                sakId = sakId,
                saksnummer = saksnummer,
                ident = ident,
                søknad = søknad,
                fødselsdato = personopplysningFødselsdato(),
                registrerteTiltak = registrerteTiltak,
                saksbehandler = ObjectMother.saksbehandler(),
            )

            behandlingRepo.lagre(behandling)

            val hentBehandling = behandlingRepo.hentOrNull(behandling.id)

            hentBehandling shouldNotBe null
        }
    }

    @Test
    fun `lagre og hente en behandling som er vilkårsvurdert`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val (_, _, behandling) = testDataHelper.persisterOpprettetFørstegangsbehandling()
            val behandlingRepo = testDataHelper.behandlingRepo

            behandling.tilstand shouldBe BehandlingTilstand.OPPRETTET
            behandlingRepo.lagre(behandling)
            behandlingRepo.hentOrNull(behandling.id) shouldBe behandling
        }
    }

    @Test
    fun `hentAlleForIdent skal kun hente behandlinger for en ident og ikke de andre`() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val behandlingRepo = testDataHelper.behandlingRepo
            val sakRepo = testDataHelper.sakRepo

            val ident = random.nextInt().toString()
            val vårSakId = SakId.random()
            val enAnnenSakId = SakId.random()
            val sakForVårIdent = sakMedOpprettetBehandling(sakId = vårSakId, ident = ident)
            val enAnnenSak = sakMedOpprettetBehandling(sakId = enAnnenSakId, ident = "random", løpenummer = 1002)

            sakRepo.lagre(sakForVårIdent)
            sakRepo.lagre(enAnnenSak)

            val hentBehandling = behandlingRepo.hentAlleForIdent(ident)

            hentBehandling.size shouldBe 1
        }
    }
}
