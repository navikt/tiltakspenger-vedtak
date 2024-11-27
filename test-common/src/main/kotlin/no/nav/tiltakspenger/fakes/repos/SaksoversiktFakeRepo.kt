package no.nav.tiltakspenger.fakes.repos

import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.FØRSTEGANGSBEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt.Behandlingstype.SØKNAD
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.ports.SaksoversiktRepo

class SaksoversiktFakeRepo(
    private val søknadFakeRepo: SøknadFakeRepo,
    private val behandlingFakeRepo: BehandlingFakeRepo,
) : SaksoversiktRepo {

    override fun hentAlle(sessionContext: SessionContext?): Saksoversikt {
        val behandlinger =
            behandlingFakeRepo.alle.filter { it.erFørstegangsbehandling }
                .associateBy { behandling ->
                    behandling.søknad!!.id
                }
        return søknadFakeRepo.alle.map { søknad ->
            val førstegangsbehandling = behandlinger[søknad.id]
            val erFørstegangsbehandling = førstegangsbehandling != null
            val status = if (erFørstegangsbehandling) {
                BehandlingEllerSøknadForSaksoversikt.Status.Behandling(førstegangsbehandling!!.status)
            } else {
                BehandlingEllerSøknadForSaksoversikt.Status.Søknad
            }
            BehandlingEllerSøknadForSaksoversikt(
                periode = førstegangsbehandling?.vurderingsperiode,
                status = status,
                underkjent = førstegangsbehandling?.attesteringer
                    ?.any { attestering -> attestering.isUnderkjent() }
                    ?: false,
                kravtidspunkt = søknad.opprettet,
                behandlingstype = if (erFørstegangsbehandling) FØRSTEGANGSBEHANDLING else SØKNAD,
                fnr = søknad.fnr,
                saksnummer = førstegangsbehandling?.saksnummer,
                id = førstegangsbehandling?.id ?: søknad.id,
                saksbehandler = førstegangsbehandling?.saksbehandler,
                beslutter = førstegangsbehandling?.beslutter,
                sakId = førstegangsbehandling?.sakId,
            )
        }.let { Saksoversikt(it) }
    }
}
