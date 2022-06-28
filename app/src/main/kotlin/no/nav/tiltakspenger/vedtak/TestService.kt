package no.nav.tiltakspenger.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.River.PacketListener

private val LOG = KotlinLogging.logger {}

class TestService(rapidsConnection: RapidsConnection) : PacketListener {
    init {
        River(rapidsConnection).apply {
            validate {
                it.requireKey("@løsning")
                it.interestedIn("@behov")
                it.interestedIn("@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info { "Mottok løsning: ${packet.toJson()}" }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        LOG.debug { problems.toExtendedReport() }
    }
}
