package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class KorrigerbartJaNeiPeriodeVilkår private constructor(
    val vilkår: Vilkår,
    val vurderingsperiode: Periode,
    val opprinneligSaksopplysning: JaNeiPeriodeSaksopplysning,
    val korrigertSaksopplysning: JaNeiPeriodeSaksopplysning?,
    val avklartSaksopplysning: JaNeiPeriodeSaksopplysning,
    val vurdering: Periodisering<Utfall>,
) {

    fun oppdaterVurderingsperiode(nyVurderingsperiode: Periode): KorrigerbartJaNeiPeriodeVilkår {
        val nyOpprinneligSaksopplysning = opprinneligSaksopplysning.oppdaterVurderingsperiode(nyVurderingsperiode)
        val nyKorrigertSaksopplysning = korrigertSaksopplysning?.oppdaterVurderingsperiode(nyVurderingsperiode)
        return this.copy(
            opprinneligSaksopplysning = nyOpprinneligSaksopplysning,
            korrigertSaksopplysning = nyKorrigertSaksopplysning,
        )
            .avklarFakta()
            .vilkårsvurder()
    }

    fun oppdaterSaksopplysning(oppdatering: OppdaterJaNeiPeriodeSaksopplysningCommand): KorrigerbartJaNeiPeriodeVilkår {
        require(oppdatering.bruker is Saksbehandler) { "Støtter ikke oppdatering av systembruker" }
        return this.copy(
            korrigertSaksopplysning = JaNeiPeriodeSaksopplysning(
                kilde = oppdatering.kilde,
                detaljer = oppdatering.detaljer,
                saksbehandler = oppdatering.bruker.navIdent,
                verdi = oppdatering.verdi,
            ),
        ).avklarFakta().vilkårsvurder()
    }

    private fun avklarFakta(): KorrigerbartJaNeiPeriodeVilkår =
        this.copy(avklartSaksopplysning = korrigertSaksopplysning ?: opprinneligSaksopplysning)

    private fun vilkårsvurder(): KorrigerbartJaNeiPeriodeVilkår =
        this.copy(vurdering = vilkårsvurder(avklartSaksopplysning))

    companion object {
        operator fun invoke(
            vilkår: Vilkår,
            vurderingsperiode: Periode,
            saksopplysning: JaNeiPeriodeSaksopplysning,
        ) = KorrigerbartJaNeiPeriodeVilkår(
            vilkår = vilkår,
            vurderingsperiode = vurderingsperiode,
            opprinneligSaksopplysning = saksopplysning,
            korrigertSaksopplysning = null,
            avklartSaksopplysning = saksopplysning,
            vurdering = vilkårsvurder(saksopplysning),
        )

        private fun vilkårsvurder(avklartSaksopplysning: JaNeiPeriodeSaksopplysning): Periodisering<Utfall> {
            return avklartSaksopplysning.verdi.map {
                when (it) {
                    // TODO: Kvalitetssikre hvilken vei det skal gå
                    JaNei.JA -> Utfall.OPPFYLT
                    JaNei.NEI -> Utfall.IKKE_OPPFYLT
                }
            }
        }
    }
}
