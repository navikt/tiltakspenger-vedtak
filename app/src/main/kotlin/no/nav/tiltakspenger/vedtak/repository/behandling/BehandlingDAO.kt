package no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.domene.vilkår.inngangsVilkår
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.objectmothers.nyVilkårsvurdering
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vilkårsvurdering.Behandling
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import org.intellij.lang.annotations.Language

class BehandlingDAO {

    fun hentForSøker(søkerId: SøkerId, txSession: TransactionalSession): List<Behandling> {
        return txSession.run(
            queryOf(hentBehandlinger, søkerId.toString())
                .map { row -> row.toBehandling() }
                .asList
        )
    }

    fun lagre(søkerId: SøkerId, behandlinger: List<Behandling>, txSession: TransactionalSession) {
        slettBehandlinger(søkerId, txSession)
        behandlinger.forEach { behandling ->
            lagreBehandling(søkerId, behandling, txSession)
        }
    }

    private fun lagreBehandling(søkerId: SøkerId, behandling: Behandling, txSession: TransactionalSession) {
        txSession.run(
            queryOf(
                lagreBehandling, mapOf(
                    "id" to behandling.id.toString(),
                    "sokerId" to søkerId.toString(),
                )
            ).asUpdate
        )
    }

    private fun slettBehandlinger(søkerId: SøkerId, txSession: TransactionalSession) {
        txSession.run(queryOf(slettBehandlinger, søkerId.toString()).asUpdate)
    }

    private fun Row.toBehandling(): Behandling {
        val id = BehandlingId.fromDb(string("id"))
        val inngangsvilkårsvurderinger: Inngangsvilkårsvurderinger = nyVilkårsvurdering()
        return Behandling(
            id = id,
            inngangsvilkårsvurderinger = inngangsvilkårsvurderinger,
        )
    }

    @Language("SQL")
    private val lagreBehandling = """
        insert into behandling (
            id,
            søker_id
        ) values (
            :id,
            :sokerId
        )""".trimIndent()

    @Language("SQL")
    private val slettBehandlinger = "delete from behandling where søker_id = ?"

    @Language("SQL")
    private val hentBehandlinger = "select * from behandling where søker_id = ?"

    companion object {
        private const val ULID_PREFIX_BEHANDLING = "behandling"
    }
}
