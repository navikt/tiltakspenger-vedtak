package no.nav.tiltakspenger.vedtak.service.ports

import no.nav.tiltakspenger.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.domene.vedtak.Vedtak

interface BrevPublisherGateway {
    fun sendBrev(vedtak: Vedtak, personopplysninger: PersonopplysningerSøker)
}
