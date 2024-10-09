package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.EnkelPerson
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway

class PersonFakeGateway : PersonGateway {
    private val data = Atomic(mutableMapOf<Fnr, PersonopplysningerSøker>())

    private val kall = Atomic(mutableListOf<Fnr>())
    val antallKall: Int get() = kall.get().size
    val alleKall: List<Fnr> get() = kall.get().toList()

    override suspend fun hentPerson(fnr: Fnr): PersonopplysningerSøker = data.get()[fnr]!!

    override suspend fun hentEnkelPerson(fnr: Fnr): EnkelPerson = data.get()[fnr]!!.let {
        EnkelPerson(
            fnr = fnr,
            fornavn = it.fornavn,
            mellomnavn = it.mellomnavn,
            etternavn = it.etternavn,
            fortrolig = it.fortrolig,
            strengtFortrolig = it.strengtFortrolig,
            strengtFortroligUtland = it.strengtFortroligUtland,
            skjermet = false,
        )
    }

    /**
     * Denne bør kalles av testoppsettet før vi lager en søknad.
     * Overskriver eksisterende personopplysninger for personen.
     */
    fun leggTilPersonopplysning(
        fnr: Fnr,
        personopplysninger: PersonopplysningerSøker,
    ) {
        data.get()[fnr] = personopplysninger
    }
}
