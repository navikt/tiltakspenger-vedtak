package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.livsoppholdsytelser.LivsoppholdVilkårData

internal class LivsoppholdVilkårDataDAO(
    private val korrigerbarLivsoppholdDAO: KorrigerbarLivsoppholdDAO = KorrigerbarLivsoppholdDAO(),
) {

    fun hent(
        behandlingId: BehandlingId,
        txSession: TransactionalSession,
    ): LivsoppholdVilkårData {
        val korrigerbarLivsopphold = korrigerbarLivsoppholdDAO.hent(behandlingId, txSession)
        return LivsoppholdVilkårData.fromDb(
            vurderingsperiode = korrigerbarLivsopphold.values.first().vurderingsperiode,
            korrigerbareYtelser = korrigerbarLivsopphold,
        )
    }

    fun lagre(
        behandlingId: BehandlingId,
        livsoppholdVilkårData: LivsoppholdVilkårData,
        txSession: TransactionalSession,
    ) {
        korrigerbarLivsoppholdDAO.slett(behandlingId, txSession)
        korrigerbarLivsoppholdDAO.lagre(behandlingId, livsoppholdVilkårData.livsoppholdYtelser.values, txSession)
    }
}
