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
    private val behandlingDao: BehandlingDAO,
    private val attesteringDao: AttesteringDAO,
    private val vedtakDao: VedtakDAO,
) : MultiRepo {
    override suspend fun lagreOgKjør(
        iverksattBehandling: BehandlingIverksatt,
        attestering: Attestering,
        vedtak: Vedtak,
        operasjon: suspend () -> String,
    ): String {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                behandlingDao.lagre(iverksattBehandling, txSession)
                attesteringDao.lagre(attestering, txSession)
                vedtakDao.lagreVedtak(vedtak, txSession)
                return@transaction operasjon()
            }
        }
    }

    override fun lagre(behandling: BehandlingVilkårsvurdert, attestering: Attestering) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                behandlingDao.lagre(behandling, txSession)
                attesteringDao.lagre(attestering, txSession)
            }
        }
    }
}
