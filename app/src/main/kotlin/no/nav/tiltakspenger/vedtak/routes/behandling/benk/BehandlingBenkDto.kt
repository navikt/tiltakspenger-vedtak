package no.nav.tiltakspenger.vedtak.routes.behandling.benk

import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.vedtak.routes.behandling.benk.BehandlingBenkDto.TypeBehandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

/**
 * @property periode Null dersom det er en søknad.
 * @property sakId Null dersom det er en søknad.
 * @property saksnummer Er med for visning i frontend. Null dersom det er en søknad.
 * @property id Unik identifikator for behandlingen. For søknader er dette søknadId. For førstegangsbehandlinger er dette behandlingId.
 */
internal data class BehandlingBenkDto(
    val periode: PeriodeDTO?,
    val status: Status,
    // val underkjent: Boolean,
    val typeBehandling: TypeBehandling,
    val ident: String,
    val saksnummer: String?,
    val id: String,
    val saksbehandler: String?,
    val beslutter: String?,
    val sakId: String?,
) {
    enum class Status {
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

    /** Skal sannsynligvis utvides med revurdering, klage og tilbakekreving. */
    enum class TypeBehandling {
        Søknad,
        Førstegangsbehandling,
    }
}

internal fun Saksoversikt.fraBehandlingToBehandlingBenkDto(): List<BehandlingBenkDto> {
    return this.map { it.toBehandlingBenkDto() }
}

internal fun BehandlingEllerSøknadForSaksoversikt.toBehandlingBenkDto(): BehandlingBenkDto {
    return BehandlingBenkDto(
        periode = periode?.toDTO(),
        status = when (status) {
            BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus.SØKNAD -> BehandlingBenkDto.Status.SØKNAD
            BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus.KLAR_TIL_BEHANDLING -> BehandlingBenkDto.Status.KLAR_TIL_BEHANDLING
            BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus.UNDER_BEHANDLING -> BehandlingBenkDto.Status.UNDER_BEHANDLING
            BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus.KLAR_TIL_BESLUTNING -> BehandlingBenkDto.Status.KLAR_TIL_BESLUTNING
            BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus.UNDER_BESLUTNING -> BehandlingBenkDto.Status.UNDER_BESLUTNING
            BehandlingEllerSøknadForSaksoversikt.Behandlingsstatus.INNVILGET -> BehandlingBenkDto.Status.INNVILGET
        },
        // TODO jah: Etter denne PRen, så kan man flytte attesteringsobjektet inn på behandling.kt så vi får tak i det her.
        // underkjent = this.be,
        typeBehandling = when (behandlingstype) {
            BehandlingEllerSøknadForSaksoversikt.Behandlingstype.SØKNAD -> Førstegangsbehandling
            BehandlingEllerSøknadForSaksoversikt.Behandlingstype.FØRSTEGANGSBEHANDLING -> Førstegangsbehandling
        },
        ident = fnr.verdi,
        saksnummer = saksnummer.toString(),
        id = id.toString(),
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        sakId = sakId.toString(),
    )
}
