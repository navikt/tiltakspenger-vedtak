package no.nav.tiltakspenger.fakes.clients

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway

class BrevPublisherGatewayFake : BrevPublisherGateway {

    private val brevSendt: Atomic<Map<SakId, BrevSendt>> = Atomic(mutableMapOf())
    val antallBrevSendt: Int get() = brevSendt.get().size

    override fun sendBrev(saksnummer: Saksnummer, vedtak: Rammevedtak, personopplysninger: PersonopplysningerSøker) {
        brevSendt.get().plus(
            vedtak.sakId to BrevSendt(saksnummer, vedtak, personopplysninger),
        )
    }

    fun hentBrevSendt(sakId: SakId): BrevSendt? {
        return brevSendt.get()[sakId]
    }

    /** Hent på sakId er raskere. */
    fun hentBrevSendt(saksnummer: Saksnummer): BrevSendt? {
        return brevSendt.get().values.find { it.saksnummer == saksnummer }
    }

    data class BrevSendt(
        val saksnummer: Saksnummer,
        val vedtak: Rammevedtak,
        val personopplysninger: PersonopplysningerSøker,
    )
}
