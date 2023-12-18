package no.nav.tiltakspenger.vedtak.service.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepo
import java.time.LocalDate
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class VedtakServiceImpl(
    private val vedtakRepo: VedtakRepo,
    private val rapidsConnection: RapidsConnection,
) : VedtakService {
    override fun hentVedtak(vedtakId: VedtakId): Vedtak? {
        return vedtakRepo.hent(vedtakId)
    }

    override fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak> {
        return vedtakRepo.hentVedtakForBehandling(behandlingId)
    }

    override fun lagVedtakForBehandling(behandling: BehandlingIverksatt): Vedtak {
        val vedtak = Vedtak(
            id = VedtakId.random(),
            behandling = behandling,
            vedtaksdato = LocalDate.now(),
            vedtaksType = if (behandling is BehandlingIverksatt.Innvilget) VedtaksType.INNVILGELSE else VedtaksType.AVSLAG,
            periode = behandling.vurderingsperiode,
            saksopplysninger = behandling.saksopplysninger(),
            vurderinger = behandling.vilkårsvurderinger,
            saksbehandler = behandling.saksbehandler!!,
            beslutter = behandling.beslutter,
        )
        val lagretVedtak = vedtakRepo.lagreVedtak(vedtak)

        val meldekortDTO = MeldekortGrunnlagDTO(
            vedtakId = lagretVedtak.id.toString(),
            behandlingId = lagretVedtak.behandling.id.toString(),
            status = when (lagretVedtak.vedtaksType) {
                VedtaksType.AVSLAG -> StatusDTO.IKKE_AKTIV
                VedtaksType.INNVILGELSE -> StatusDTO.AKTIV
                VedtaksType.STANS -> StatusDTO.IKKE_AKTIV
                VedtaksType.FORLENGELSE -> StatusDTO.AKTIV
            },
            vurderingsperiode = PeriodeDTO(fra = lagretVedtak.periode.fra, til = lagretVedtak.periode.til),
            tiltak = lagretVedtak.behandling.tiltak
                .filter { it.id == lagretVedtak.behandling.søknad().tiltak.id }
                .map {
                    TiltakDTO(
                        periodeDTO = PeriodeDTO(fra = it.deltakelseFom, til = it.deltakelseTom),
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

        mutableMapOf(
            "@event_name" to "meldekortGrunnlag",
            "@opprettet" to LocalDateTime.now(),
            "meldekortGrunnlag" to meldekortDTO,
        ).let { JsonMessage.newMessage(it) }
            .also { message ->
                SECURELOG.info { "Vi sender grunnlag : ${message.toJson()}" }
                rapidsConnection.publish(lagretVedtak.id.toString(), message.toJson())
            }
        return lagretVedtak
    }
}

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
