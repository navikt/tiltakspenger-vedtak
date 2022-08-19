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
import no.nav.tiltakspenger.vedtak.Personinfo
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.meldinger.PersondataMottattHendelse
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PersondataMottattRiver(
    private val søkerMediator: SøkerMediator,
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
                it.demandAllOrAny("@behov", listOf("persondata"))
                it.demandKey("@løsning")
                it.requireKey("ident")
                it.requireKey("@opprettet")
                it.interestedIn("@løsning.persondata.person")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info("Received persondata")
        SECURELOG.info("Received persondata for ident id: ${packet["ident"].asText()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val persondataMottattHendelse = PersondataMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["ident"].asText(),
            personinfo = mapPersoninfo(
                personinfoDTO = packet["@løsning.persondata.person"].asObject(PersoninfoDTO::class.java),
                innhentet = packet["@opprettet"].asLocalDateTime(),
                ident = packet["ident"].asText(),
            )
        )

        søkerMediator.håndter(persondataMottattHendelse)
    }

    //Hvorfor finnes ikke dette i r&r?
    private fun <T> JsonNode?.asObject(clazz: Class<T>): T = objectMapper.treeToValue(this, clazz)

    private fun mapPersoninfo(personinfoDTO: PersoninfoDTO, innhentet: LocalDateTime, ident: String): Personinfo {
        return Personinfo(
            ident = ident,
            fødselsdato = personinfoDTO.fødselsdato,
            fornavn = personinfoDTO.fornavn,
            mellomnavn = personinfoDTO.mellomnavn,
            etternavn = personinfoDTO.etternavn,
            fortrolig = if (personinfoDTO.adressebeskyttelseGradering == AdressebeskyttelseGradering.FORTROLIG) true else false,
            strengtFortrolig = if (personinfoDTO.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG || personinfoDTO.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND) true else false,
            innhentet = innhentet,
        )
    }
}



