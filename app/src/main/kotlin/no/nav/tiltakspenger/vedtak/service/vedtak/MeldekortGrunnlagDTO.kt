package no.nav.tiltakspenger.vedtak.service.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.domene.vedtak.VedtaksType
import java.time.LocalDate
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class MeldekortGrunnlagDTO(
    val vedtakId: String,
    val sakId: String,
    val behandlingId: String,
    val status: StatusDTO,
    val vurderingsperiode: PeriodeDTO,
    val tiltak: List<TiltakDTO>,
    val personopplysninger: PersonopplysningerDTO,
    val utfallsperioder: List<UtfallsperiodeDTO>,
)

data class UtfallsperiodeDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    val antallBarn: Int,
    val utfall: UtfallForPeriodeDTO,
)

enum class UtfallForPeriodeDTO {
    GIR_RETT_TILTAKSPENGER,
    GIR_IKKE_RETT_TILTAKSPENGER,
    KREVER_MANUELL_VURDERING,
}

data class PersonopplysningerDTO(
    val fornavn: String,
    val etternavn: String,
    val ident: String,
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

private fun mapMeldekortGrunnlagDTO(vedtak: Vedtak) =
    MeldekortGrunnlagDTO(
        vedtakId = vedtak.id.toString(),
        sakId = vedtak.sakId.toString(),
        behandlingId = vedtak.behandling.id.toString(),
        status = when (vedtak.vedtaksType) {
            VedtaksType.AVSLAG -> StatusDTO.IKKE_AKTIV
            VedtaksType.INNVILGELSE -> StatusDTO.AKTIV
            VedtaksType.STANS -> StatusDTO.IKKE_AKTIV
            VedtaksType.FORLENGELSE -> StatusDTO.AKTIV
        },
        vurderingsperiode = PeriodeDTO(
            fra = vedtak.periode.fra,
            til = vedtak.periode.til,
        ),
        tiltak = mapTiltakDTO(vedtak),
        personopplysninger = PersonopplysningerDTO(
            fornavn = vedtak.behandling.søknad().personopplysninger.fornavn,
            etternavn = vedtak.behandling.søknad().personopplysninger.etternavn,
            ident = vedtak.behandling.søknad().personopplysninger.ident,
        ),
        utfallsperioder = vedtak.utfallsperioder.map {
            UtfallsperiodeDTO(
                fom = it.fom,
                tom = it.tom,
                antallBarn = it.antallBarn,
                utfall = when (it.utfall) {
                    UtfallForPeriode.GIR_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER
                    UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER
                    UtfallForPeriode.KREVER_MANUELL_VURDERING -> UtfallForPeriodeDTO.KREVER_MANUELL_VURDERING
                },

            )
        },
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
