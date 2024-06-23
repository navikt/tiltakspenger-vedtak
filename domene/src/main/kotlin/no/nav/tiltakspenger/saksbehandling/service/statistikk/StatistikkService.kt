package no.nav.tiltakspenger.saksbehandling.service.statistikk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface StatistikkService {
    fun opprettBehandlingTilDvh(sak: Sak, behandling: Førstegangsbehandling)
    fun iverksattBehandlingTilDvh(sak: Sak, vedtak: Vedtak)
}
