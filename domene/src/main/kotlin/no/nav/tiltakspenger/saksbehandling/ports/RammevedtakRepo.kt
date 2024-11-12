package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.distribusjon.domene.DistribusjonId
import no.nav.tiltakspenger.distribusjon.domene.VedtakSomSkalDistribueres
import no.nav.tiltakspenger.felles.journalføring.JournalpostId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import java.time.LocalDateTime

interface RammevedtakRepo {
    fun hentForVedtakId(vedtakId: VedtakId): Rammevedtak?

    fun lagre(
        vedtak: Rammevedtak,
        context: TransactionContext? = null,
    )

    fun hentForFnr(fnr: Fnr): List<Rammevedtak>

    fun hentRammevedtakSomSkalJournalføres(limit: Int = 10): List<Rammevedtak>

    fun hentRammevedtakSomSkalDistribueres(limit: Int = 10): List<VedtakSomSkalDistribueres>

    fun markerJournalført(id: VedtakId, journalpostId: JournalpostId, tidspunkt: LocalDateTime)

    fun markerDistribuert(id: VedtakId, distribusjonId: DistribusjonId, tidspunkt: LocalDateTime)

    fun hentRammevedtakTilDatadeling(limit: Int = 10): List<Rammevedtak>

    fun markerSendtTilDatadeling(id: VedtakId, tidspunkt: LocalDateTime)
}
