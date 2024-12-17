package no.nav.tiltakspenger.vedtak.repository.benk

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.objectmothers.ObjectMother
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.benk.BenkBehandlingstype
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.vedtak.db.persisterOpprettetFørstegangsbehandling
import no.nav.tiltakspenger.vedtak.db.persisterSøknad
import no.nav.tiltakspenger.vedtak.db.withMigratedDb
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SaksoversiktPostgresRepoTest {
    @Test
    fun hentAlle() {
        withMigratedDb(runIsolated = true) { testDataHelper ->
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
                                behandlingstype = BenkBehandlingstype.SØKNAD,
                                fnr = søknad1.fnr,
                                saksnummer = null,
                                saksbehandler = null,
                                beslutter = null,
                                sakId = null,
                                underkjent = false,
                                kravtidspunkt = LocalDateTime.from(1.januarDateTime(2022)),
                                id = søknad1.id,
                            ),
                            BehandlingEllerSøknadForSaksoversikt(
                                periode = ObjectMother.vurderingsperiode(),
                                status = BehandlingEllerSøknadForSaksoversikt.Status.Behandling(Behandlingsstatus.UNDER_BEHANDLING),
                                behandlingstype = BenkBehandlingstype.FØRSTEGANGSBEHANDLING,
                                fnr = søknad2.fnr,
                                saksnummer = sak.saksnummer,
                                saksbehandler = sak.førstegangsbehandling.saksbehandler!!,
                                beslutter = null,
                                sakId = sak.id,
                                underkjent = false,
                                kravtidspunkt = LocalDateTime.from(1.januarDateTime(2022)),
                                id = sak.førstegangsbehandling.id,
                            ),
                        ),
                    )
            }
        }
    }
}
