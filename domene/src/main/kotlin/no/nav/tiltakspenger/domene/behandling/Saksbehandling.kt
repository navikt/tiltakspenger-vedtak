package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.Søknad

interface Saksbehandling {
    fun behandle(søknad: Søknad)
}
