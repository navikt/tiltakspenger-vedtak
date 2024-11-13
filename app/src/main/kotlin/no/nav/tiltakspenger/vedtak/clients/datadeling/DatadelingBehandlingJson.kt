package no.nav.tiltakspenger.vedtak.clients.datadeling

import no.nav.tiltakspenger.libs.json.serialize
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

private data class DatadelingBehandlingJson(
    val behandlingId: String,
    val sakId: String,
    val saksnummer: String,
)

fun Førstegangsbehandling.toBehandlingJson(): String {
    return DatadelingBehandlingJson(
        behandlingId = this.id.toString(),
        sakId = this.sakId.toString(),
        saksnummer = this.saksnummer.verdi,
        // TODO pre-mvp: Legg på de feltene som mangler.
    ).let { serialize(it) }
}
