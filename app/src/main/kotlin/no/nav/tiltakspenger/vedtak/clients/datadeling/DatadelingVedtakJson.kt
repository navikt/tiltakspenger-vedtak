package no.nav.tiltakspenger.vedtak.clients.datadeling

import no.nav.tiltakspenger.libs.json.serialize
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

private data class DatadelingVedtakJson(
    val vedtakId: String,
    val sakId: String,
    val saksnummer: String,
)

fun Rammevedtak.toDatadelingJson(): String {
    return DatadelingVedtakJson(
        vedtakId = this.id.toString(),
        sakId = this.sakId.toString(),
        saksnummer = this.saksnummer.verdi,
        // TODO pre-mvp: Legg på de feltene som mangler.
    ).let { serialize(it) }
}
