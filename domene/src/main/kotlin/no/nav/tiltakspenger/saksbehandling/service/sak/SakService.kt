package no.nav.tiltakspenger.saksbehandling.service.sak

import arrow.core.Either
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.FantIkkeFnr
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.KanIkkeStarteFørstegangsbehandling

interface SakService {
    fun startFørstegangsbehandling(
        søknadId: SøknadId,
        saksbehandler: Saksbehandler,
    ): Either<KanIkkeStarteFørstegangsbehandling, Sak>

    fun hentForFørstegangsbehandlingId(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
    ): Sak

    fun hentForSaksnummer(
        saksnummer: Saksnummer,
        saksbehandler: Saksbehandler,
    ): Sak

    fun hentForFnr(
        fnr: Fnr,
        saksbehandler: Saksbehandler,
    ): Either<FantIkkeFnr, Sak>

    fun hentFnrForSakId(sakId: SakId): Fnr?

    fun hentForSakId(
        sakId: SakId,
        saksbehandler: Saksbehandler,
    ): Sak?

    fun hentSaksoversikt(saksbehandler: Saksbehandler): Saksoversikt
}
