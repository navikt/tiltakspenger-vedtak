package no.nav.tiltakspenger.saksbehandling.service.ports

import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface BrevPublisherGateway {
    fun sendBrev(vedtak: Vedtak, personopplysninger: PersonopplysningerSøker)
}
