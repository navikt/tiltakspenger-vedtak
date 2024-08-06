package no.nav.tiltakspenger.vedtak.clients.meldekort

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.ports.MeldekortGrunnlagGateway
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class MeldekortGrunnlagGatewayImpl(
    private val rapidsConnection: RapidsConnection,
) : MeldekortGrunnlagGateway {
    override fun sendMeldekortGrunnlag(
        sak: SakDetaljer,
        vedtak: Vedtak,
    ) {
        mutableMapOf(
            "@event_name" to "meldekortGrunnlag",
            "@opprettet" to LocalDateTime.now(),
            "meldekortGrunnlag" to MeldekortGrunnlagDTOMapper.mapMeldekortGrunnlagDTO(sak, vedtak),
        ).let { JsonMessage.newMessage(it) }
            .also { message ->
                SECURELOG.info { "Vi sender grunnlag : ${message.toJson()}" }
                rapidsConnection.publish(vedtak.id.toString(), message.toJson())
            }
    }
}
