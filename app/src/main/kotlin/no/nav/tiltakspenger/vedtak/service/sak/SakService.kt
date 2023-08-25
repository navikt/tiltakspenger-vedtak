package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad

interface SakService {
    fun motta(søknad: Søknad): Sak
    fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak
}
