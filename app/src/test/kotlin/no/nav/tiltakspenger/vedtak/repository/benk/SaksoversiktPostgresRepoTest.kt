package no.nav.tiltakspenger.vedtak.repository.benk

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus.UNDER_BEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.FØRSTEGANGSBEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.SØKNAD
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.vedtak.db.TestDataHelper
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.persisterSøknad
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test

class SaksoversiktPostgresRepoTest {

    @Test
    fun hentAlle() {
        withMigratedDb { dataSource ->
            val testDataHelper = TestDataHelper(dataSource)
            val repo = testDataHelper.saksoversiktRepo
            val søknad1 = testDataHelper.persisterSøknad()
            val (sak, søknad2) = testDataHelper.persisterOpprettetFørstegangsbehandling()
            repo.hentAlle().also {
                it shouldBe Saksoversikt(
                    listOf(
                        BehandlingEllerSøknadForSaksoversikt(
                            periode = null,
                            status = Behandlingsstatus.SØKNAD,
                            behandlingstype = SØKNAD,
                            fnr = søknad1.personopplysninger.fnr,
                            saksnummer = null,
                            saksbehandler = null,
                            beslutter = null,
                            sakId = null,
                            id = søknad1.id,
                        ),
                        BehandlingEllerSøknadForSaksoversikt(
                            periode = Periode(1.januar(2023), 31.mars(2023)),
                            status = UNDER_BEHANDLING,
                            behandlingstype = FØRSTEGANGSBEHANDLING,
                            fnr = søknad2.personopplysninger.fnr,
                            saksnummer = sak.saksnummer,
                            saksbehandler = sak.førstegangsbehandling.saksbehandler!!,
                            beslutter = null,
                            sakId = sak.id,
                            id = sak.førstegangsbehandling.id,
                        ),
                    ),
                )
            }
        }
    }
}
