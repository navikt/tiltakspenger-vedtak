package no.nav.tiltakspenger.vedtak.rivers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse

internal class SøknadMottakRiver(
    private val søkerMediator: SøkerMediator,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        private val logg = KotlinLogging.logger {}
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "søknad_mottatt")
                it.requireKey("@id")
                it.requireKey("søknad")
                it.requireKey("søknad.ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        logg.info("Received søknad for ident id: ${packet["søknad.ident"].asText()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["søknad.ident"].asText(),
            søknad = packet["søknad"].asObject(Søknad::class.java)
        )

        søkerMediator.håndter(søknadMottattHendelse)
    }

    //Hvorfor finnes ikke dette i r&r?
    private fun JsonNode?.asObject(clazz: Class<Søknad>): Søknad = objectMapper.treeToValue(this, clazz)

}


