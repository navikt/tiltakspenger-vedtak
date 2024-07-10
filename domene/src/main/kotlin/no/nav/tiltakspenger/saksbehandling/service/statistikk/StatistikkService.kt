package no.nav.tiltakspenger.saksbehandling.service.statistikk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface StatistikkService {
    fun opprettBehandlingTilDvh(sak: SakDetaljer, behandling: Førstegangsbehandling)
    fun iverksattBehandlingTilDvh(sak: SakDetaljer, behandling: Behandling, vedtak: Vedtak)
}
