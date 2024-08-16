package no.nav.tiltakspenger.saksbehandling.domene.benk

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.Ulid
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandlingsstatus
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import java.time.LocalDateTime

/**
 * En oversikt over flere søknader og behandlinger på tvers av saker.
 */
data class Saksoversikt(
    val behandlinger: List<BehandlingEllerSøknadForSaksoversikt>,
) : List<BehandlingEllerSøknadForSaksoversikt> by behandlinger

/**
 * @property id Vi har ikke en fellestype for behandlingId og søknadId, så vi bruker Ulid. Hvis ikke må vi endre denne til en sealed interface.
 */
data class BehandlingEllerSøknadForSaksoversikt(
    val periode: Periode?,
    val status: Status,
    val underkjent: Boolean?,
    val kravtidspunkt: LocalDateTime,
    val behandlingstype: Behandlingstype,
    val fnr: Fnr,
    val saksnummer: Saksnummer?,
    val id: Ulid,
    val saksbehandler: String?,
    val beslutter: String?,
    val sakId: SakId?,
    val kanBehandles: Boolean?,
) {
    sealed interface Status {
        data object Søknad : Status

        data class Behandling(
            val behandlingsstatus: Behandlingsstatus,
        ) : Status
    }

    enum class Behandlingstype {
        SØKNAD,
        FØRSTEGANGSBEHANDLING,
    }
}
