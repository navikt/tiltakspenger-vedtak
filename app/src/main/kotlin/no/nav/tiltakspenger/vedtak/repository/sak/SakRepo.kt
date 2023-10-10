package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.felles.Periode

interface SakRepo {
    fun hentForIdentMedPeriode(fnr: String, periode: Periode): List<Sak>
    fun lagre(sak: Sak): Sak
}
