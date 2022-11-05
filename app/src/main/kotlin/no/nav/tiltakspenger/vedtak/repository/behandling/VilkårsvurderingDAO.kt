package no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.objectmothers.nyVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import org.intellij.lang.annotations.Language

class VilkårsvurderingDAO {

    fun hentForBehandling(
        behandlingId: BehandlingId,
        txSession: TransactionalSession
    ): List<Inngangsvilkårsvurderinger> {
        return txSession.run(
            queryOf(hentVilkårsvurderinger, behandlingId.toString())
                .map { row -> row.toVilkårsvurdering() }
                .asList
        )
    }

    fun lagre(
        behandlingId: BehandlingId,
        vilkårsvurderinger: List<Inngangsvilkårsvurderinger>,
        txSession: TransactionalSession
    ) {
        slettVilkårsvurderinger(behandlingId, txSession)
        vilkårsvurderinger.forEach { vilkårsvurdering ->
            lagreVilkårsvurdering(behandlingId, vilkårsvurdering, txSession)
        }
    }

    private fun lagreVilkårsvurdering(
        behandlingId: BehandlingId,
        inngangsvilkårsvurderinger: Inngangsvilkårsvurderinger,
        txSession: TransactionalSession
    ) {
        txSession.run(
            queryOf(
                lagreVilkårsvurdering, mapOf(
                    "id" to behandlingId.toString(),
                    "behandlingId" to behandlingId.toString(),
                )
            ).asUpdate
        )
    }

    private fun slettVilkårsvurderinger(behandlingId: BehandlingId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettVilkårsvurdering, behandlingId.toString()).asUpdate)
    }

    private fun Row.toVilkårsvurdering(): Inngangsvilkårsvurderinger {
        val id = BehandlingId.fromDb(string("id"))
        val inngangsvilkårsvurderinger: Inngangsvilkårsvurderinger = nyVilkårsvurdering()
        return nyVilkårsvurdering()
    }

    @Language("SQL")
    private val lagreVilkårsvurdering = """
        insert into behandling (
            id,
            søker_id
        ) values (
            :id,
            :sokerId
        )""".trimIndent()

    @Language("SQL")
    private val slettVilkårsvurdering = "delete from behandling where søker_id = ?"

    @Language("SQL")
    private val hentVilkårsvurderinger = "select * from behandling where søker_id = ?"

    companion object {
        private const val ULID_PREFIX_BEHANDLING = "behandling"
    }
}
