package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.VilkårData

class VilkårDataRepo(
    private val ytelseVilkårDAO: YtelseVilkårDAO = YtelseVilkårDAO(),
) {
    fun lagre(behandlingId: BehandlingId, ytelseVilkår: VilkårData, txSession: TransactionalSession) {
        ytelseVilkårDAO.lagre(behandlingId, ytelseVilkår.ytelse, txSession)
    }

    fun hent(behandlingId: BehandlingId, vurderingsperiode: Periode, txSession: TransactionalSession): VilkårData {
        return VilkårData(ytelse = ytelseVilkårDAO.hentYtelseVilkår(behandlingId, vurderingsperiode = vurderingsperiode, txSession))
    }
}
