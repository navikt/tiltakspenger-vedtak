package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.StatistikkSakDTO

interface StatistikkSakRepo {
    fun lagre(dto: StatistikkSakDTO)
}
