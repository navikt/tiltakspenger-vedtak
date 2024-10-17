package no.nav.tiltakspenger.saksbehandling.service.vedtak

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

interface RammevedtakService {
    fun hentVedtak(vedtakId: VedtakId): Rammevedtak?
    fun hentVedtakForFnr(fnr: Fnr, periode: Periode): List<Rammevedtak>
}
