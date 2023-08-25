package no.nav.tiltakspenger.vedtak.repository.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.felles.Periode

class PostgresSakRepo() : SakRepo {
    override fun findByFnrAndPeriode(fnr: String, periode: Periode): List<Sak> {
        TODO("Not yet implemented")
    }

    override fun save(sak: Sak): Sak {
        TODO("Not yet implemented")
    }
}
