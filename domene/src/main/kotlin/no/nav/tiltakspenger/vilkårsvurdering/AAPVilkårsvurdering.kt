package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import java.time.LocalDate

class AAPVilkårsvurdering(
    ytelser: List<YtelseSak>,
    vurderingsperiode: Periode,
    private val baseManuellOgAutomatiskVilkårsvurdering: BaseManuellOgAutomatiskVilkårsvurdering = BaseManuellOgAutomatiskVilkårsvurdering(
        automatiskVilkårsvurdering = BaseStatligYtelseVilkårsvurdering(
            ytelser = ytelser,
            vurderingsperiode = vurderingsperiode,
            type = YtelseSak.YtelseSakYtelsetype.AA,
        )
    )
) : IVilkårsvurdering,
    IManuellVilkårsvurdering,
    IStatligVilkårsvurdering,
    Vilkårsvurdering() {

    override val lovreferanse: Lovreferanse = Lovreferanse.AAP
    override fun samletUtfall(): Utfall = baseManuellOgAutomatiskVilkårsvurdering.samletUtfall()

    override fun vurderinger(): List<Vurdering> = baseManuellOgAutomatiskVilkårsvurdering.vurderinger()

    override fun manuellVurdering(): Vurdering? = baseManuellOgAutomatiskVilkårsvurdering.manuellVurdering()

    override fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String) {
        baseManuellOgAutomatiskVilkårsvurdering.settManuellVurdering(
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer
        )
    }
}
