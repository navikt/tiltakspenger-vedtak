package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway

class TiltakFakeGateway : TiltakGateway {
    private val data = Atomic(mutableMapOf<Fnr, List<Tiltak>>())

    override suspend fun hentTiltak(fnr: Fnr): List<Tiltak> = data.get()[fnr]!!

    fun lagre(
        fnr: Fnr,
        tiltak: Tiltak,
    ) {
        data.get()[fnr] = listOf(tiltak)
    }
}
