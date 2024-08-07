package no.nav.tiltakspenger.vedtak.repository.benk

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
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
                it shouldBe
                    Saksoversikt(
                        listOf(
                            BehandlingEllerSøknadForSaksoversikt(
                                periode = null,
                                status = BehandlingEllerSøknadForSaksoversikt.Status.Søknad,
                                behandlingstype = SØKNAD,
                                fnr = søknad1.personopplysninger.fnr,
                                saksnummer = null,
                                saksbehandler = null,
                                beslutter = null,
                                sakId = null,
                                id = søknad1.id,
                            ),
                            BehandlingEllerSøknadForSaksoversikt(
                                periode = ObjectMother.vurderingsperiode(),
                                status = BehandlingEllerSøknadForSaksoversikt.Status.Behandling(Behandlingsstatus.UNDER_BEHANDLING),
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
