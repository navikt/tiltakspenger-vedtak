package no.nav.tiltakspenger.vedtak.repository.multi

import kotliquery.sessionOf
import no.nav.tiltakspenger.domene.attestering.Attestering
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.vedtak.VedtakRepo
import no.nav.tiltakspenger.vedtak.service.ports.AttesteringRepo
import no.nav.tiltakspenger.vedtak.service.ports.BehandlingRepo
import no.nav.tiltakspenger.vedtak.service.ports.MultiRepo

class MultiRepoImpl(
    private val behandlingRepo: BehandlingRepo,
    private val attesteringRepo: AttesteringRepo,
    private val vedtakRepo: VedtakRepo,
) : MultiRepo {
    override suspend fun <T> lagreOgKjør(
        iverksattBehandling: BehandlingIverksatt,
        attestering: Attestering,
        vedtak: Vedtak,
        operasjon: suspend () -> T,
    ): T {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                behandlingRepo.lagre(iverksattBehandling, txSession)
                attesteringRepo.lagre(attestering, txSession)
                vedtakRepo.lagreVedtak(vedtak, txSession)
                return@transaction operasjon()
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
