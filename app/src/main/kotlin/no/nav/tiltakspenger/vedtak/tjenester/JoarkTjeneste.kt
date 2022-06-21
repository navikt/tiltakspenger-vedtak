package no.nav.tiltakspenger.vedtak.tjenester

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.meldinger.JoarkHendelse

internal class JoarkMottak(
    private val innsendingMediator: InnsendingMediator,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        private val logg = KotlinLogging.logger {}

        private val forbudteMottaksKanaler = setOf<String>(
            "EESSI",
            "NAV_NO_CHAT"
        )
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireKey("journalpostId") }
            validate { it.requireKey("journalpostStatus") }
            validate { it.requireValue("temaNytt", "DAG") }
            validate { it.requireAny("hendelsesType", listOf("MidlertidigJournalført", "JournalpostMottatt")) }
            validate {
                it.require("mottaksKanal") { mottaksKanal ->
                    val kanal = mottaksKanal.asText()
                    if (kanal in forbudteMottaksKanaler) throw IllegalArgumentException("Kan ikke håndtere '$kanal' mottakskanal")
                }
            }
            validate { it.interestedIn("temaNytt", "hendelsesType", "mottaksKanal", "behandlingstema") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        logg.info(
            """Received journalpost with journalpost id: ${packet["journalpostId"].asText()} 
              |tema: ${packet["temaNytt"].asText()}, 
              |hendelsesType: ${packet["hendelsesType"].asText()}, 
              |mottakskanal, ${packet["mottaksKanal"].asText()}, 
              |behandlingstema: ${packet["behandlingstema"].asText()}
              |journalpostStatus: ${packet["journalpostStatus"].asText()}
              |""".trimMargin()
        )

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val joarkHendelse = JoarkHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = packet["journalpostId"].asText(),
            hendelseType = packet["hendelsesType"].asText(),
            journalpostStatus = packet["journalpostStatus"].asText(),
            behandlingstema = packet["behandlingstema"].asText() ?: null
        )

        innsendingMediator.håndter(joarkHendelse)
    }
}