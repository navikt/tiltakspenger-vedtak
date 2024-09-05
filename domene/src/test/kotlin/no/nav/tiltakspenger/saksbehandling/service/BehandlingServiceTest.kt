package no.nav.tiltakspenger.saksbehandling.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.fakes.repos.BehandlingFakeRepo
import no.nav.tiltakspenger.fakes.repos.PersonopplysningerFakeRepo
import no.nav.tiltakspenger.objectmothers.ObjectMother.behandlingTilBeslutterInnvilget
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandlerUtenTilgang
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.servicemothers.ServiceMother.withBehandlingService
import org.junit.jupiter.api.Test

internal class BehandlingServiceTest {

    @Test
    fun `må ha beslutterrolle for å ta behandling som er til beslutning`() {
        val behandling = behandlingTilBeslutterInnvilget()
        val behandlingId = behandling.id
        val sakPersonopplysninger = SakPersonopplysninger(
            listOf(personopplysningKjedeligFyr(fnr = behandling.fnr)),
        )
        withBehandlingService(
            behandlingRepo = BehandlingFakeRepo(behandling),
            personopplysningerRepo = PersonopplysningerFakeRepo(behandling.sakId to sakPersonopplysninger),
        ) { service, _ ->
            shouldThrow<IllegalStateException> {
                service.taBehandling(behandlingId, saksbehandlerUtenTilgang())
            }.message shouldBe
                "Saksbehandler må ha beslutterrolle. Utøvende saksbehandler: Saksbehandler(navIdent='Z12345', brukernavn='*****', epost='*****', roller=Roller(value=[]))"
            shouldNotThrow<IllegalStateException> {
                service.taBehandling(behandlingId, beslutter())
            }
        }
    }

    @Test
    fun `sjekk at man ikke kan sende tilbake uten beslutter rolle`() {
        val navIdentSaksbehandler = "A12345"
        val saksbehandler = saksbehandler(navIdent = navIdentSaksbehandler)
        val navIdentBeslutter = "B12345"
        val beslutter = beslutter(navIdent = navIdentBeslutter)
        val behandling = behandlingTilBeslutterInnvilget(saksbehandler).taBehandling(beslutter)
        val behandlingId = behandling.id
        val sakPersonopplysninger = SakPersonopplysninger(
            listOf(personopplysningKjedeligFyr(fnr = behandling.fnr)),
        )
        withBehandlingService(
            behandlingRepo = BehandlingFakeRepo(behandling),
            personopplysningerRepo = PersonopplysningerFakeRepo(behandling.sakId to sakPersonopplysninger),
        ) { service, _ ->
            shouldThrow<IllegalStateException> {
                service.taBehandling(behandlingId, saksbehandlerUtenTilgang(navIdent = navIdentBeslutter))
            }.message shouldBe
                "Saksbehandler må ha beslutterrolle. Utøvende saksbehandler: Saksbehandler(navIdent='B12345', brukernavn='*****', epost='*****', roller=Roller(value=[]))"

            shouldNotThrow<IllegalStateException> {
                service.sendTilbakeTilSaksbehandler(
                    behandlingId,
                    beslutter(navIdent = navIdentBeslutter),
                    "begrunnelse",
                )
            }
        }
    }
}
