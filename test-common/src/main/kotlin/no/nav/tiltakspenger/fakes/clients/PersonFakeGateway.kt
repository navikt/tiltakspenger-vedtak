package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway

class PersonFakeGateway : PersonGateway {
    private val data = Atomic(mutableMapOf<Fnr, List<Personopplysninger>>())

    private val kall = Atomic(mutableListOf<Fnr>())
    val antallKall: Int get() = kall.get().size
    val alleKall: List<Fnr> get() = kall.get().toList()

    override suspend fun hentPerson(fnr: Fnr): List<Personopplysninger> = data.get()[fnr]!!

    /**
     * Denne bør kalles av testoppsettet før vi lager en søknad.
     * Overskriver eksisterende personopplysninger for personen.
     */
    fun leggTilPersonopplysning(
        fnr: Fnr,
        personopplysninger: List<Personopplysninger>,
    ) {
        data.get()[fnr] = personopplysninger
    }
}
