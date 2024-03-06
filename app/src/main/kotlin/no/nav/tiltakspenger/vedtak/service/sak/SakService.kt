package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.felles.BehandlingId

interface SakService {
    fun motta(søknad: Søknad): Sak
    fun henteMedBehandlingsId(behandlingId: BehandlingId): Sak?
}
