package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.ports.SkjermingGateway

class SkjermingFakeGateway : SkjermingGateway {
    private val data = Atomic((mutableMapOf<Fnr, Boolean>()))

    /** Feiler dersom personen ikke finnes i datastettet */
    override suspend fun erSkjermetPerson(fnr: Fnr): Boolean = data.get()[fnr]!!

    fun lagre(
        fnr: Fnr,
        skjermet: Boolean,
    ) {
        data.get()[fnr] = skjermet
    }

    fun leggTil(
        fnr: Fnr,
        skjermet: Boolean,
    ) {
        data.get().putIfAbsent(fnr, skjermet)
    }
}
