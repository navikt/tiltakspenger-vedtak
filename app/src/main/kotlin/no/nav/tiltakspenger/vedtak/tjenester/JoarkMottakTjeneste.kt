package no.nav.tiltakspenger.vedtak.tjenester

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.meldinger.JoarkHendelse

internal class JoarkMottakTjeneste(
    private val søkerMediator: SøkerMediator,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        private val logg = KotlinLogging.logger {}
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.requireAllOrAny("@event", listOf("journalpost"))
                it.requireKey("@id")
                it.requireKey("ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        logg.info(
            """Received journalpost with journalpost id: ${packet["ident"].asText()} 
              |""".trimMargin()
        )

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val joarkHendelse = JoarkHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["ident"].asText(),
        )

        søkerMediator.håndter(joarkHendelse)
    }
}