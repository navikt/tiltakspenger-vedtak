package no.nav.tiltakspenger.saksbehandling.service.sak

import arrow.core.Either
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saker
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling

interface SakService {
    fun startFørstegangsbehandling(søknadId: SøknadId, saksbehandler: Saksbehandler): Either<KanIkkeStarteFørstegangsbehandling, Sak>
    fun hentMedBehandlingIdOrNull(behandlingId: BehandlingId): Sak?
    fun hentMedBehandlingId(behandlingId: BehandlingId, saksbehandler: Saksbehandler): Sak
    fun hentForIdent(ident: String, saksbehandler: Saksbehandler): Saker
    fun hentForSaksnummer(saksnummer: String, saksbehandler: Saksbehandler): Sak
}
