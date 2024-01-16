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

data class MeldekortGrunnlagDTO(
    val vedtakId: String,
    val behandlingId: String,
    val status: StatusDTO,
    val vurderingsperiode: PeriodeDTO,
    val tiltak: List<TiltakDTO>,
)

enum class StatusDTO {
    AKTIV,
    IKKE_AKTIV,
}

data class TiltakDTO(
    val periodeDTO: PeriodeDTO,
    val typeBeskrivelse: String,
    val typeKode: String,
    val antDagerIUken: Float,
)

data class PeriodeDTO(
    val fra: LocalDate,
    val til: LocalDate,
)

fun sendMeldekortGrunnlag(vedtak: Vedtak, rapidsConnection: RapidsConnection) {
    mutableMapOf(
        "@event_name" to "meldekortGrunnlag",
        "@opprettet" to LocalDateTime.now(),
        "meldekortGrunnlag" to mapMeldekortGrunnlagDTO(vedtak),
    ).let { JsonMessage.newMessage(it) }
        .also { message ->
            SECURELOG.info { "Vi sender grunnlag : ${message.toJson()}" }
            rapidsConnection.publish(vedtak.id.toString(), message.toJson())
        }
}

private fun mapMeldekortGrunnlagDTO(lagretVedtak: Vedtak) =
    MeldekortGrunnlagDTO(
        vedtakId = lagretVedtak.id.toString(),
        behandlingId = lagretVedtak.behandling.id.toString(),
        status = when (lagretVedtak.vedtaksType) {
            VedtaksType.AVSLAG -> StatusDTO.IKKE_AKTIV
            VedtaksType.INNVILGELSE -> StatusDTO.AKTIV
            VedtaksType.STANS -> StatusDTO.IKKE_AKTIV
            VedtaksType.FORLENGELSE -> StatusDTO.AKTIV
        },
        vurderingsperiode = PeriodeDTO(
            fra = lagretVedtak.periode.fra,
            til = lagretVedtak.periode.til,
        ),
        tiltak = lagretVedtak.behandling.tiltak
            .filter { it.id == lagretVedtak.behandling.søknad().tiltak.id }
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
            },
    )
