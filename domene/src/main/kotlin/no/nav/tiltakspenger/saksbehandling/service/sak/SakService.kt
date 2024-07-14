package no.nav.tiltakspenger.saksbehandling.service.sak

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak

interface SakService {
    fun motta(søknad: Søknad): Sak
    fun hentMedBehandlingIdOrNull(behandlingId: BehandlingId): Sak?
    fun hentMedBehandlingId(behandlingId: BehandlingId, saksbehandler: Saksbehandler): Sak
    fun hentForIdent(ident: String, saksbehandler: Saksbehandler): List<Sak>
    fun hentForSaksnummer(saksnummer: String, saksbehandler: Saksbehandler): Sak
    fun resettLøpenr()
    fun oppdaterPersonopplysninger(sakId: SakId): Sak
}
