package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface MeldekortGrunnlagGateway {
    fun sendMeldekortGrunnlag(sak: Sak, vedtak: Vedtak)
}
