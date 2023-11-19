package no.nav.tiltakspenger.vedtak.service.vedtak

import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.domene.vedtak.VedtaksType
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepo
import java.time.LocalDate

class VedtakServiceImpl(
    private val vedtakRepo: VedtakRepo,
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
        return vedtakRepo.lagreVedtak(vedtak)
    }
}
