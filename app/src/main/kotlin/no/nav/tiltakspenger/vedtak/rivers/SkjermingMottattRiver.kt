package no.nav.tiltakspenger.vedtak.rivers

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.meldinger.SkjermingMottattHendelse

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class SkjermingMottattRiver(rapidsConnection: RapidsConnection, private val søkerMediator: SøkerMediator) :
    River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("skjerming"))
                it.demandKey("@løsning")
                it.requireKey("ident")
                it.requireKey("@opprettet")
                it.interestedIn("@løsning.skjerming")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info("Received skjerming")
        SECURELOG.info("Received skjerming for ident id: ${packet["ident"].asText()}")

        val skjermingMottattHendelse = SkjermingMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["ident"].asText(),
            skjerming = Skjerming(
                ident = packet["ident"].asText(),
                skjerming = packet["@løsning.skjerming"].asBoolean(),
                innhentet = packet["@opprettet"].asLocalDateTime()
            )

        )

        søkerMediator.håndter(skjermingMottattHendelse)
    }
}
