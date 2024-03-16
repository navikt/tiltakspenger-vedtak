package no.nav.tiltakspenger.vedtak.repository.multi

import kotliquery.sessionOf
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.ports.MultiRepo
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.attestering.AttesteringDAO
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingDAO
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakDAO

class MultiRepoImpl(
    private val behandlingRepo: BehandlingDAO,
    private val attesteringRepo: AttesteringDAO,
    private val vedtakRepo: VedtakDAO,
) : MultiRepo {
    override fun <T> lagreOgKjør(
        iverksattBehandling: BehandlingIverksatt,
        attestering: Attestering,
        vedtak: Vedtak,
        operasjon: () -> T,
    ): T {
        sessionOf(DataSource.hikariDataSource).use {
            return it.transaction { txSession ->
                behandlingRepo.lagre(iverksattBehandling, txSession)
                attesteringRepo.lagre(attestering, txSession)
                vedtakRepo.lagreVedtak(vedtak, txSession)
                operasjon()
            }
        }
    }

    override fun lagre(behandling: BehandlingVilkårsvurdert, attestering: Attestering) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                behandlingRepo.lagre(behandling, txSession)
                attesteringRepo.lagre(attestering, txSession)
            }
        }
    }
}
