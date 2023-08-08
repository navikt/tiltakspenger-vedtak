package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.Sak
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad


interface SakService {
    fun motta(søknad: Søknad)
    fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak
}
