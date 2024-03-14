package no.nav.tiltakspenger.vedtak.service.ports

import no.nav.tiltakspenger.domene.vedtak.Vedtak

interface MeldekortGrunnlagGateway {
    fun sendMeldekortGrunnlag(vedtak: Vedtak)
}
