package no.nav.tiltakspenger.vedtak.routes.behandling.benk

import no.nav.tiltakspenger.saksbehandling.domene.benk.BehandlingEllerSøknadForSaksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.vedtak.routes.behandling.benk.BehandlingBenkDto.TypeBehandling.Førstegangsbehandling
import no.nav.tiltakspenger.vedtak.routes.behandling.benk.BehandlingBenkDto.TypeBehandling.Søknad
import no.nav.tiltakspenger.vedtak.routes.behandling.toDTO
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
    val status: String,
    // val underkjent: Boolean,
    val typeBehandling: TypeBehandling,
    val ident: String,
    val saksnummer: String?,
    val id: String,
    val saksbehandler: String?,
    val beslutter: String?,
    val sakId: String?,
) {
    /** Skal sannsynligvis utvides med revurdering, klage og tilbakekreving. */
    enum class TypeBehandling {
        Søknad,
        Førstegangsbehandling,
    }
}

internal fun Saksoversikt.fraBehandlingToBehandlingBenkDto(): List<BehandlingBenkDto> = this.map { it.toBehandlingBenkDto() }

internal fun BehandlingEllerSøknadForSaksoversikt.toBehandlingBenkDto(): BehandlingBenkDto =
    BehandlingBenkDto(
        periode = periode?.toDTO(),
        status =
        when (val s = status) {
            is BehandlingEllerSøknadForSaksoversikt.Status.Søknad -> "SØKNAD"
            is BehandlingEllerSøknadForSaksoversikt.Status.Behandling -> s.behandlingsstatus.toDTO().toString()
        },
        // TODO jah: Etter denne PRen, så kan man flytte attesteringsobjektet inn på behandling.kt så vi får tak i det her.
        // underkjent = this.be,
        typeBehandling =
        when (behandlingstype) {
            BehandlingEllerSøknadForSaksoversikt.Behandlingstype.SØKNAD -> Søknad
            BehandlingEllerSøknadForSaksoversikt.Behandlingstype.FØRSTEGANGSBEHANDLING -> Førstegangsbehandling
        },
        ident = fnr.verdi,
        saksnummer = saksnummer.toString(),
        id = id.toString(),
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        sakId = sakId.toString(),
    )
