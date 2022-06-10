package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.helse.rapids_rivers.River.PacketListener

private val LOG = KotlinLogging.logger {}

class TestService(rapidsConnection: RapidsConnection) : PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.rejectKey("@løsning")
                it.interestedIn("@behov")
                it.interestedIn("@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info { packet.toJson() }
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        LOG.error { error }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        LOG.error { problems }
    }
}
