package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway

class PersonGatewayFake(
    private val data: Map<Fnr, List<Personopplysninger>>,
) : PersonGateway {
    private val kall = Atomic(mutableListOf<Fnr>())
    val antallKall: Int get() = kall.get().size
    val alleKall: List<Fnr> get() = kall.get().toList()
    override suspend fun hentPerson(fnr: Fnr): List<Personopplysninger> {
        return data[fnr]!!
    }
}
