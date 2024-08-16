package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad

interface SøknadRepo {
    fun lagre(
        søknad: Søknad,
        kanBehandles: Boolean,
        txContext: TransactionContext? = null,
    )

    fun hentSøknad(søknadId: SøknadId): Søknad
}
