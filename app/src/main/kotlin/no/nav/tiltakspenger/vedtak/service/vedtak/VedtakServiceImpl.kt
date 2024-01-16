package no.nav.tiltakspenger.vedtak.service.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepo
import java.time.LocalDate

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

        sendMeldekortGrunnlag(lagretVedtak, rapidsConnection)

        return lagretVedtak
    }
}
