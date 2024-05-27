package no.nav.tiltakspenger.vedtak.clients.brevpublisher

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.ports.BrevPublisherGateway
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class BrevPublisherGatewayImpl(
    private val rapidsConnection: RapidsConnection,
) : BrevPublisherGateway {

    override fun sendBrev(
        saksnummer: Saksnummer,
        vedtak: Vedtak,
        personopplysninger: PersonopplysningerSøker,
    ) {
        mutableMapOf(
            "@event_name" to "vedtaksbrev",
            "@opprettet" to LocalDateTime.now(),
            "vedtaksbrev" to VedtaksbrevMapper.mapVedtaksBrevDTO(saksnummer, vedtak, personopplysninger),
        ).let { JsonMessage.newMessage(it) }
            .also { message ->
                SECURELOG.info { "Vi sender vedtaksbrev : ${message.toJson()}" }
                rapidsConnection.publish(vedtak.id.toString(), message.toJson())
            }
    }
}
