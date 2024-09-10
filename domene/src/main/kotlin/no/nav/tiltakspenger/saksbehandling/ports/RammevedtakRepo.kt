package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

interface RammevedtakRepo {
    fun hentForVedtakId(vedtakId: VedtakId): Rammevedtak?

    fun lagre(
        vedtak: Rammevedtak,
        context: TransactionContext? = null,
    )

    fun hentForFnr(fnr: Fnr): List<Rammevedtak>
}
