package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingRepo {
    fun lagre(behandling: Behandling, context: TransactionContext? = null): Behandling
    fun hentOrNull(behandlingId: BehandlingId): Behandling?
    fun hent(behandlingId: BehandlingId): Behandling
    fun hentAlle(): List<Førstegangsbehandling>
    fun hentAlleForIdent(ident: String): List<Førstegangsbehandling>
}
