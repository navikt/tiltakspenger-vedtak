package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkStønadDTO

interface StatistikkStønadRepo {
    fun lagre(dto: StatistikkStønadDTO)
}
