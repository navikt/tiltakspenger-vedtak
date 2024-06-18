package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.livsoppholdsytelser.LivsoppholdVilkår

internal class LivsoppholdVilkårDataDAO(
    private val korrigerbarLivsoppholdDAO: KorrigerbarLivsoppholdDAO = KorrigerbarLivsoppholdDAO(),
) {

    fun hent(
        behandlingId: BehandlingId,
        txSession: TransactionalSession,
    ): LivsoppholdVilkår {
        val korrigerbarLivsopphold = korrigerbarLivsoppholdDAO.hent(behandlingId, txSession)
        return LivsoppholdVilkår.fromDb(
            vurderingsperiode = korrigerbarLivsopphold.values.first().vurderingsperiode,
            korrigerbareYtelser = korrigerbarLivsopphold,
        )
    }

    fun lagre(
        behandlingId: BehandlingId,
        livsoppholdVilkår: LivsoppholdVilkår,
        txSession: TransactionalSession,
    ) {
        korrigerbarLivsoppholdDAO.slett(behandlingId, txSession)
        korrigerbarLivsoppholdDAO.lagre(behandlingId, livsoppholdVilkår.livsoppholdYtelser.values, txSession)
    }
}
