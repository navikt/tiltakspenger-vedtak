package no.nav.tiltakspenger.vedtak.repository.barnetillegg

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.barnetillegg.BarnetilleggVilkårData

class BarnetilleggDAO {
    fun hent(
        behandlingId: BehandlingId,
        txSession: TransactionalSession,
        vurderingsperiode: Periode,
    ): BarnetilleggVilkårData {
        // TODO: Fikse både henting og lagring
        return BarnetilleggVilkårData(vurderingsperiode)
    }
}
