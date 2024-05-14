package no.nav.tiltakspenger.vedtak.repository.behandling

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import kotliquery.sessionOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.objectmothers.ObjectMother.sakMedOpprettetBehandling
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayCleanAndMigrate
import no.nav.tiltakspenger.vedtak.repository.sak.PostgresSakRepo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class YtelseVilkårDAOTest {
    private val ytelseVilkårDAO = YtelseVilkårDAO()
    private val sakRepo = PostgresSakRepo()

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeEach
    fun setup() {
        flywayCleanAndMigrate()
    }

    @Test
    fun `lagre og hente Ytelsevilkår`() {
        val periode = Periode(fra = 1.januar(2023), til = 31.mars(2023))
        val sak = sakMedOpprettetBehandling(periode = periode)
        sakRepo.lagre(sak)
        val behandlingId = sak.behandlinger.single().id

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                val ytelseVilkår = ytelseVilkårDAO.hentYtelseVilkår(behandlingId = behandlingId, vurderingsperiode = periode, txSession = txSession)
                ytelseVilkår.aap.avklarteSaksopplysninger shouldBe null
                ytelseVilkår.aap.saksopplysningerAnnet shouldBe null
                ytelseVilkår.aap.saksopplysningerSaksbehandler shouldBe null
                ytelseVilkår.aap.vurderingsperiode shouldBe periode
            }
        }
    }

    @Test
    fun `lagre og hente Ytelsevilkår gg`() {
        val periode = Periode(fra = 1.januar(2023), til = 31.mars(2023))
        val sak = sakMedOpprettetBehandling(periode = periode)
        sakRepo.lagre(sak)
        val behandlingId = sak.behandlinger.single().id

        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                val ytelseVilkår = ytelseVilkårDAO.hentYtelseVilkår(behandlingId = behandlingId, vurderingsperiode = periode, txSession = txSession)
                ytelseVilkår.aap.avklarteSaksopplysninger shouldBe  null


                ytelseVilkårDAO.lagre(behandlingId = behandlingId, ytelseVilkår = ytelseVilkår, txSession = txSession)
            }
        }
    }
}
