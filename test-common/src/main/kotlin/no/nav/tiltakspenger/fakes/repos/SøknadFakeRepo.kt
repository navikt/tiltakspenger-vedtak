package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo

class SøknadFakeRepo(
    private val data: Atomic<MutableMap<SøknadId, Søknad>> = Atomic(mutableMapOf()),
) : SøknadRepo {

    constructor(data: Map<SøknadId, Søknad>) : this(Atomic(data.toMutableMap()))

    constructor(vararg søknader: Søknad) : this(søknader.associateBy { it.id })
    constructor(eksisterendeSøknader: List<Søknad>) : this(eksisterendeSøknader.associateBy { it.id })

    val alle get() = data.get().values.toList()

    override fun lagre(søknad: Søknad, txContext: TransactionContext?) {
        data.get()[søknad.id] = søknad
    }

    override fun hentSøknad(søknadId: SøknadId): Søknad {
        return data.get()[søknadId]!!
    }
}
