package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.domene.Sak
import no.nav.tiltakspenger.felles.Periode

interface SakRepo {
    fun findByFnrAndPeriode(fnr: String, periode: Periode): List<Sak>
    fun save(sak: Sak): Sak
}
