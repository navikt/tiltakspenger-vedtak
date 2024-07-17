package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.service.statistikk.SakStatistikkDTO

interface StatistikkSakRepo {
    fun lagre(dto: SakStatistikkDTO)
}
