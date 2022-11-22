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
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import no.nav.tiltakspenger.vedtak.repository.søker.SøkerRepository
import no.nav.tiltakspenger.vedtak.rivers.SøknadDTO.Companion.mapSøknad

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class SøknadMottattRiver(
    private val innsendingMediator: InnsendingMediator,
    private val søkerRepository: SøkerRepository,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
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
                it.requireKey("@opprettet")
                it.requireKey("søknad")
                it.requireKey("søknad.journalpostId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info("Received søknad")
        SECURELOG.info("Received søknad: ${packet.toJson()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val søknad = mapSøknad(
            dto = packet["søknad"].asObject(SøknadDTO::class.java),
            innhentet = packet["@opprettet"].asLocalDateTime()
        )
        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = packet["søknad.journalpostId"].asText(),
            søknad = søknad
        )

        innsendingMediator.håndter(søknadMottattHendelse)
        // TODO: Dette er et hack:
        søkerRepository.lagre(Søker(ident = søknad.ident))
    }

    //Hvorfor finnes ikke dette i r&r?
    private fun <T> JsonNode?.asObject(clazz: Class<T>): T = objectMapper.treeToValue(this, clazz)


}
