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
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class ArenaTiltakMottattRiver(
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
                it.demandAllOrAny("@behov", listOf("arenatiltak"))
                it.demandKey("@løsning")
                it.requireKey("ident")
                it.requireKey("@opprettet")
                it.interestedIn("@løsning.arenatiltak")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info("Received arenatiltak")
        SECURELOG.info("Received arenatiltak for ident id: ${packet["ident"].asText()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val arenaTiltakMottattHendelse = ArenaTiltakMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["ident"].asText(),
            innhentet = packet["@opprettet"].asLocalDateTime(),
            tiltaksaktivitet = mapArenaTiltak(
                tiltaksaktivitetDTO = packet["@løsning.arenatiltak"].asList(),
                innhentet = packet["@opprettet"].asLocalDateTime(),
            )
        )

        søkerMediator.håndter(arenaTiltakMottattHendelse)
    }

    fun JsonNode?.asList(): List<TiltaksaktivitetDTO> {
        var javaType: CollectionType = objectMapper.getTypeFactory()
            .constructCollectionType(List::class.java, TiltaksaktivitetDTO::class.java)

        return objectMapper.treeToValue(this, javaType)
    }

    private fun mapArenaTiltak(
        tiltaksaktivitetDTO: List<TiltaksaktivitetDTO>,
        innhentet: LocalDateTime
    ): List<Tiltaksaktivitet> {
        return tiltaksaktivitetDTO.map {
            Tiltaksaktivitet(
                tiltaksnavn = it.tiltaksnavn,
                aktivitetId = it.aktivitetId,
                tiltakLokaltNavn = it.tiltakLokaltNavn,
                arrangoer = it.arrangoer,
                bedriftsnummer = it.bedriftsnummer,
                deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(
                    it.deltakelsePeriode?.fom,
                    it.deltakelsePeriode?.tom
                ),
                deltakelseProsent = it.deltakelseProsent,
                deltakerStatus = Tiltaksaktivitet.DeltakerStatus(
                    termnavn = it.deltakerStatus.termnavn,
                    status = it.deltakerStatus.innerText
                ),
                statusSistEndret = it.statusSistEndret,
                begrunnelseInnsoeking = it.begrunnelseInnsoeking,
                antallDagerPerUke = it.antallDagerPerUke,
                innhentet = innhentet,
            )
        }
    }
}