package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class KorrigerbartJaNeiVilkår private constructor(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val opprinneligSaksopplysning: JaNeiSaksopplysning,
    val korrigertSaksopplysning: JaNeiSaksopplysning?,
    val avklartSaksopplysning: JaNeiSaksopplysning,
    val vurdering: Periodisering<Utfall>,
) : JaNeiPeriodeVurdering {

    override fun vurdering(): Periodisering<Utfall> = vurdering

    fun oppdaterSaksopplysning(oppdatering: OppdaterJaNeiSaksopplysningCommand): KorrigerbartJaNeiVilkår {
        require(oppdatering.bruker is Saksbehandler) { "Støtter ikke oppdatering av systembruker" }
        return this.copy(
            korrigertSaksopplysning = JaNeiSaksopplysning(
                kilde = oppdatering.kilde,
                detaljer = oppdatering.detaljer,
                saksbehandler = oppdatering.bruker.navIdent,
                verdi = oppdatering.verdi,
            ),
        ).avklarFakta().vilkårsvurder()
    }

    private fun avklarFakta(): KorrigerbartJaNeiVilkår =
        this.copy(avklartSaksopplysning = korrigertSaksopplysning ?: opprinneligSaksopplysning)

    private fun vilkårsvurder(): KorrigerbartJaNeiVilkår =
        this.copy(vurdering = vilkårsvurder(vurderingsperiode, avklartSaksopplysning))

    companion object {
        operator fun invoke(
            vilkår: Vilkår,
            vurderingsperiode: Periode,
            opprinneligSaksopplysning: JaNeiSaksopplysning,
        ) = KorrigerbartJaNeiVilkår(
            vilkår = vilkår,
            vurderingsperiode = vurderingsperiode,
            opprinneligSaksopplysning = opprinneligSaksopplysning,
            korrigertSaksopplysning = null,
            avklartSaksopplysning = opprinneligSaksopplysning,
            vurdering = vilkårsvurder(vurderingsperiode, opprinneligSaksopplysning),
        )

        private fun vilkårsvurder(
            vurderingsperiode: Periode,
            saksopplysning: JaNeiSaksopplysning,
        ): Periodisering<Utfall> =
            when (saksopplysning.verdi) {
                // TODO: Kvalitetssikre hvilken vei det skal gå
                JaNei.JA -> Periodisering(Utfall.OPPFYLT, vurderingsperiode)
                JaNei.NEI -> Periodisering(Utfall.IKKE_OPPFYLT, vurderingsperiode)
            }
    }
}
