package no.nav.tiltakspenger.vedtak.repository.søker

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.objectmothers.nySøknadMedArenaTiltak
import no.nav.tiltakspenger.objectmothers.søkerMedSøknad
import no.nav.tiltakspenger.vedtak.db.PostgresTestcontainer
import no.nav.tiltakspenger.vedtak.db.flywayMigrate
import no.nav.tiltakspenger.vilkårsvurdering.Behandling
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.util.*

@Testcontainers
internal class LagreBehandlingTest {


    private val søkerRepo = PostgresSøkerRepository()

    companion object {
        @Container
        val postgresContainer = PostgresTestcontainer
    }

    @BeforeAll
    fun setup() {
        flywayMigrate()
    }


    @Test
    fun `Skal kunne lagre og hente opp vilkårsvurderinger`() {
        val ident = Random().nextInt().toString()
        val søknad = nySøknadMedArenaTiltak(ident = ident)
        val søker = søkerMedSøknad(ident = ident, søknad = søknad)
        val vilkårsvurderinger = søker.vilkårsvurderinger(søknad.søknadId)!!
        vilkårsvurderinger.statligeYtelser.sykepenger.settManuellVurdering(
            fom = LocalDate.MIN,
            tom = LocalDate.MAX,
            utfall = Utfall.OPPFYLT,
            detaljer = "detaljer"
        )
        val behandling = Behandling(
            id = BehandlingId.random(),
            inngangsvilkårsvurderinger = søker.vilkårsvurderinger(søknad.søknadId)!!
        )
        søker.behandlinger = listOf(behandling)

        søkerRepo.lagre(søker)

        val hentetSøker = søkerRepo.hent(ident)!!
        hentetSøker.behandlinger.size shouldBe 1
        hentetSøker.behandlinger.first().inngangsvilkårsvurderinger.vurderinger() shouldContainExactly behandling.inngangsvilkårsvurderinger.vurderinger()
    }
}
