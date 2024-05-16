package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

data class KorrigerbartJaNeiVilkår private constructor(
    val vilkår: Vilkår,
    val opprinneligSaksopplysning: JaNeiSaksopplysning,
    val korrigertSaksopplysning: JaNeiSaksopplysning?,
    val avklartSaksopplysning: JaNeiSaksopplysning,
    val vurdering: Utfall,
) {
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
        this.copy(vurdering = vilkårsvurder(avklartSaksopplysning))

    companion object {
        operator fun invoke(
            vilkår: Vilkår,
            opprinneligSaksopplysning: JaNeiSaksopplysning,
        ) = KorrigerbartJaNeiVilkår(
            vilkår = vilkår,
            opprinneligSaksopplysning = opprinneligSaksopplysning,
            korrigertSaksopplysning = null,
            avklartSaksopplysning = opprinneligSaksopplysning,
            vurdering = vilkårsvurder(opprinneligSaksopplysning),
        )

        private fun vilkårsvurder(saksopplysning: JaNeiSaksopplysning): Utfall =
            when (saksopplysning.verdi) {
                // TODO: Kvalitetssikre hvilken vei det skal gå
                JaNei.JA -> Utfall.OPPFYLT
                JaNei.NEI -> Utfall.IKKE_OPPFYLT
            }
    }
}
