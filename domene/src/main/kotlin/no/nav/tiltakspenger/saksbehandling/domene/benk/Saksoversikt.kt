package no.nav.tiltakspenger.saksbehandling.domene.benk

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Ulid
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer

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
    val status: Behandlingsstatus,
    // TODO jah: val underkjent: Boolean, (må legges til på behandling)
    val behandlingstype: Behandlingstype,
    val fnr: Fnr,
    val saksnummer: Saksnummer?,
    val id: Ulid,
    val saksbehandler: String?,
    val beslutter: String?,
    val sakId: SakId?,
) {
    // TODO jah + kew: Denne slettes og arver enumen til Behandling+legge til en for søknad
    enum class Behandlingsstatus {
        /** Vi har mottatt en ny søknad. */
        SØKNAD,

        /** Det står ikke en saksbehandler på behandlingen. Kan også være underkjent dersom en saksbehandler har meldt seg av behandlignen. */
        KLAR_TIL_BEHANDLING,

        /** En saksbehandler står på behandlingen. Kan også være underkjent. */
        UNDER_BEHANDLING,

        /** Saksbehandler har sendt til beslutning, men ingen beslutter er knyttet til behandlingen enda */
        KLAR_TIL_BESLUTNING,

        /** En beslutter har tatt behandlingen. */
        UNDER_BESLUTNING,
        INNVILGET,
    }

    // TODO jah: Denne slettes og arver enumen til Behandlingstype+legge til en for søknad
    enum class Behandlingstype {
        SØKNAD,
        FØRSTEGANGSBEHANDLING,
    }
}
