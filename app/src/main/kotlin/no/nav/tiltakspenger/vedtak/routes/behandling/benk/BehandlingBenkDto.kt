package no.nav.tiltakspenger.vedtak.routes.behandling.benk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand.IVERKSATT
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand.OPPRETTET
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand.TIL_BESLUTTER
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand.UNDER_BEHANDLING
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
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

internal fun List<Behandling>.fraBehandlingToBehandlingBenkDto(): List<BehandlingBenkDto> {
    return this.map { it.toBehandlingBenkDto() }
}

internal fun Behandling.toBehandlingBenkDto(): BehandlingBenkDto {
    return BehandlingBenkDto(
        periode = vurderingsperiode.toDTO(),
        status = toBehandlingBenkDtoStatus(),
        // TODO jah: Etter denne PRen, så kan man flytte attesteringsobjektet inn på behandling.kt så vi får tak i det her.
        // underkjent = this.be,
        typeBehandling = Førstegangsbehandling,
        ident = fnr.verdi,
        saksnummer = saksnummer.toString(),
        id = id.toString(),
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        sakId = sakId.toString(),
    )
}

private fun Behandling.toBehandlingBenkDtoStatus(): BehandlingBenkDto.Status {
    return when (tilstand) {
        OPPRETTET, UNDER_BEHANDLING -> {
            if (saksbehandler == null) {
                BehandlingBenkDto.Status.KLAR_TIL_BEHANDLING
            } else {
                BehandlingBenkDto.Status.UNDER_BEHANDLING
            }
        }

        TIL_BESLUTTER -> {
            if (beslutter == null) {
                BehandlingBenkDto.Status.KLAR_TIL_BESLUTNING
            } else {
                BehandlingBenkDto.Status.UNDER_BESLUTNING
            }
        }

        IVERKSATT -> BehandlingBenkDto.Status.INNVILGET
    }
}
internal fun List<Søknad>.fraSøknadToBehandlingBenkDto(): List<BehandlingBenkDto> {
    return this.map { it.toBehandlingBenkDto() }
}

internal fun Søknad.toBehandlingBenkDto(): BehandlingBenkDto {
    return BehandlingBenkDto(
        periode = null,
        status = BehandlingBenkDto.Status.SØKNAD,
        typeBehandling = BehandlingBenkDto.TypeBehandling.Søknad,
        ident = this.personopplysninger.fnr.verdi,
        saksnummer = null,
        id = id.toString(),
        saksbehandler = null,
        beslutter = null,
        sakId = null,
    )
}
