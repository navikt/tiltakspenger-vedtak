package no.nav.tiltakspenger.innsending.ports

import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse

interface InnsendingMediator {
    fun håndter(hendelse: InnsendingHendelse)
}
