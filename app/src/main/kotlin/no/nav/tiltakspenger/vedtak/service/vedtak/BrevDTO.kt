package no.nav.tiltakspenger.vedtak.service.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.domene.vedtak.VedtaksType
import java.time.LocalDate
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class BrevDTO(
    val vedtakId: String,
    val vedtaksdato: LocalDate,
    val vedtaksType: VedtaksTypeDTO,
    val periode: PeriodeDTO,
    val saksbehandler: String,
    val beslutter: String,
    val tiltak: List<TiltakDTO>,
)

enum class VedtaksTypeDTO(val navn: String, val skalSendeBrev: Boolean) {
    AVSLAG("Avslag", true),
    INNVILGELSE("Innvilgelse", true),
    STANS("Stans", true),
    FORLENGELSE("Forlengelse", true),
}

fun sendBrev(vedtak: Vedtak, rapidsConnection: RapidsConnection) {
    mutableMapOf(
        "@event_name" to "vedtaksbrev",
        "@opprettet" to LocalDateTime.now(),
        "vedtaksbrev" to mapVedtaksBrevDTO(vedtak),
    ).let { JsonMessage.newMessage(it) }
        .also { message ->
            SECURELOG.info { "Vi sender vedtaksbrev : ${message.toJson()}" }
            rapidsConnection.publish(vedtak.id.toString(), message.toJson())
        }
}

private fun mapVedtaksBrevDTO(vedtak: Vedtak) =
    BrevDTO(
        vedtakId = vedtak.id.toString(),
        vedtaksdato = vedtak.vedtaksdato,
        vedtaksType = when (vedtak.vedtaksType) {
            VedtaksType.AVSLAG -> VedtaksTypeDTO.AVSLAG
            VedtaksType.INNVILGELSE -> VedtaksTypeDTO.INNVILGELSE
            VedtaksType.STANS -> VedtaksTypeDTO.STANS
            VedtaksType.FORLENGELSE -> VedtaksTypeDTO.FORLENGELSE
        },
        periode = PeriodeDTO(
            fra = vedtak.periode.fra,
            til = vedtak.periode.til,
        ),
        saksbehandler = vedtak.saksbehandler,
        beslutter = vedtak.beslutter,
        tiltak = mapTiltakDTO(vedtak),
    )

fun mapTiltakDTO(vedtak: Vedtak) =
    vedtak.behandling.tiltak
        .filter { it.id == vedtak.behandling.søknad().tiltak.id }
        .map {
            TiltakDTO(
                periodeDTO = PeriodeDTO(
                    fra = it.deltakelseFom,
                    til = it.deltakelseTom,
                ),
                typeBeskrivelse = it.gjennomføring.typeNavn,
                typeKode = it.gjennomføring.typeKode,
                antDagerIUken = it.deltakelseDagerUke
                    ?: if (it.deltakelseProsent == 100F) {
                        5F
                    } else {
                        throw IllegalStateException("Kan ikke beregne antall dager i uken for tiltak uten deltakelseDagerUke eller deltakelseProsent")
                    },
            )
        }
