package no.nav.tiltakspenger.vedtak.rivers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class ArenaYtelserMottattRiver(
    private val søkerMediator: SøkerMediator,
    rapidsConnection: RapidsConnection,
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
                it.demandAllOrAny("@behov", listOf("arenaytelser"))
                it.demandKey("@løsning")
                it.requireKey("ident")
                it.requireKey("@opprettet")
                it.interestedIn("@løsning.arenaytelser")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info("Received arenaytelser")
        SECURELOG.info("Received arenaytelser for ident id: ${packet["ident"].asText()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val ytelserMottattHendelse = YtelserMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["ident"].asText(),
            ytelseSak = mapYtelser(
                ytelseSakDTO = packet["@løsning.arenaytelser"].asList(),
                innhentet = packet["@opprettet"].asLocalDateTime(),
            )
        )

        søkerMediator.håndter(ytelserMottattHendelse)
    }

    fun JsonNode?.asList(): List<YtelseSakDTO> {
        var javaType: CollectionType = objectMapper.getTypeFactory()
            .constructCollectionType(List::class.java, YtelseSakDTO::class.java)

        return objectMapper.treeToValue(this, javaType)
    }

    private fun mapYtelser(
        ytelseSakDTO: List<YtelseSakDTO>,
        innhentet: LocalDateTime
    ): List<YtelseSak> {
        return ytelseSakDTO.map {
            YtelseSak(
                fomGyldighetsperiode = it.fomGyldighetsperiode,
                tomGyldighetsperiode = it.tomGyldighetsperiode,
                datoKravMottatt = it.datoKravMottatt,
                dataKravMottatt = it.dataKravMottatt,
                fagsystemSakId = it.fagsystemSakId,
                status = it.status,
                ytelsestype = it.ytelsestype,
                vedtak = it.vedtak.map {
                    YtelseSak.YtelseVedtak(
                        beslutningsDato = it.beslutningsDato,
                        periodetypeForYtelse = it.periodetypeForYtelse,
                        vedtaksperiodeFom = it.vedtaksperiodeFom,
                        vedtaksperiodeTom = it.vedtaksperiodeTom,
                        vedtaksType = it.vedtaksType,
                        status = it.status,
                    )
                },
                antallDagerIgjen = it.antallDagerIgjen,
                antallUkerIgjen = it.antallUkerIgjen,
                innhentet = innhentet,
            )
        }
    }
}