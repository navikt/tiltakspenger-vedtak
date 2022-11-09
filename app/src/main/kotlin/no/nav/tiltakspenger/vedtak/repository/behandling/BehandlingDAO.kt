package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vilkårsvurdering.Behandling
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.kategori.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AlderspensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.GjenlevendepensjonVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.LønnsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OmsorgspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OpplæringspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OvergangsstønadVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PensjonsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SupplerendeStønadVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SvangerskapspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SykepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.UføretrygdVilkårsvurdering
import org.intellij.lang.annotations.Language

internal class BehandlingDAO(
    private val vurderingDAO: VurderingDAO = VurderingDAO(),
) {

    fun hentForSøker(søkerId: SøkerId, txSession: TransactionalSession): List<Behandling> {
        return txSession.run(
            queryOf(hentBehandlinger, søkerId.toString())
                .map { row ->
                    row.toBehandling(txSession)
                }.asList
        )
    }

    fun lagre(søkerId: SøkerId, behandlinger: List<Behandling>, txSession: TransactionalSession) {
        slettBehandlinger(søkerId, txSession)
        behandlinger.forEach { behandling ->
            lagreBehandling(søkerId, behandling, txSession)
            vurderingDAO.lagre(
                behandlingId = behandling.id,
                vilkårsvurderinger = behandling.inngangsvilkårsvurderinger,
                txSession = txSession
            )
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

    private fun Row.toBehandling(tx: TransactionalSession): Behandling {
        val id = BehandlingId.fromDb(string("id"))
        val vurderinger = vurderingDAO.hentForBehandling(id, tx)

        val inngangsvilkårsvurderinger = Inngangsvilkårsvurderinger(
            statligeYtelser = StatligeYtelserVilkårsvurderingKategori(
                aap = AAPVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.AAP }),
                dagpenger = DagpengerVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.DAGPENGER }),
                sykepenger = SykepengerVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.SYKEPENGER }),
                uføretrygd = UføretrygdVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.UFØRETRYGD }),
                overgangsstønad = OvergangsstønadVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.OVERGANGSSTØNAD }),
                pleiepenger = PleiepengerVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.PLEIEPENGER }),
                foreldrepenger = ForeldrepengerVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.FORELDREPENGER }),
                svangerskapspenger = SvangerskapspengerVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.SVANGERSKAPSPENGER }),
                gjenlevendepensjon = GjenlevendepensjonVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.GJENLEVENDEPENSJON }),
                supplerendeStønad = SupplerendeStønadVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.SUPPLERENDESTØNAD }),
                alderspensjon = AlderspensjonVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.ALDERSPENSJON }),
                opplæringspenger = OpplæringspengerVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.OPPLÆRINGSPENGER }),
                omsorgspenger = OmsorgspengerVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.OMSORGSPENGER }),
            ),
            kommunaleYtelser = KommunaleYtelserVilkårsvurderingKategori(
                intro = IntroProgrammetVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.INTROPROGRAMMET }),
                kvp = KVPVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.KVP }),
            ),
            pensjonsordninger = PensjonsinntektVilkårsvurderingKategori(
                pensjonsinntektVilkårsvurdering = PensjonsinntektVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.PENSJONSINNTEKT })
            ),
            lønnsinntekt = LønnsinntektVilkårsvurderingKategori(
                lønnsinntektVilkårsvurdering = LønnsinntektVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.LØNNSINNTEKT })
            ),
            institusjonopphold = InstitusjonVilkårsvurderingKategori(
                institusjonsoppholdVilkårsvurdering = InstitusjonsoppholdVilkårsvurdering(vurderinger.filter { it.vilkår == Vilkår.INSTITUSJONSOPPHOLD }),
            )
        )
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
