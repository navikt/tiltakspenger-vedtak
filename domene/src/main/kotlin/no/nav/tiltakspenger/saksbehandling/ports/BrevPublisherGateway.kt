package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface BrevPublisherGateway {
    fun sendBrev(
        saksnummer: Saksnummer,
        vedtak: Vedtak,
        personopplysninger: PersonopplysningerSøker,
    )
}
