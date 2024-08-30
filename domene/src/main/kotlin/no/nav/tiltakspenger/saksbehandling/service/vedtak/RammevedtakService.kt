package no.nav.tiltakspenger.saksbehandling.service.vedtak

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import java.time.LocalDate

interface RammevedtakService {
    fun hentVedtak(vedtakId: VedtakId): Rammevedtak?
    fun hentVedtakForIdent(ident: Fnr, fom: LocalDate, tom: LocalDate): List<Rammevedtak>
}
