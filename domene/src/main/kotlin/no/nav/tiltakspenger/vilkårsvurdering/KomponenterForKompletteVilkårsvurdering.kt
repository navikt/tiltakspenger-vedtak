package no.nav.tiltakspenger.vilkårsvurdering

interface IKomplettVilkårsvurdering {

    fun samletUtfall(): Utfall

    fun vurderinger(): List<Vurdering>
}

class KomplettManuellVilkårsvurderingerKomponent(
    private val manuellVilkårsvurdering: DelvisManuellVilkårsvurderingKomponent
) : IKomplettVilkårsvurdering,
    IDelvisManuellVilkårsvurdering by manuellVilkårsvurdering {

    override fun samletUtfall(): Utfall = manuellVurdering()?.utfall ?: Utfall.KREVER_MANUELL_VURDERING // ?

    override fun vurderinger(): List<Vurdering> = listOfNotNull(manuellVurdering())
}

class KomplettManuellOgAutomatiskVilkårsvurderingKomponent(
    private val manuellVilkårsvurdering: IDelvisManuellVilkårsvurdering = DelvisManuellVilkårsvurderingKomponent(),
    private val automatiskVilkårsvurdering: IDelvisAutomatiskVilkårsvurdering,
) : IKomplettVilkårsvurdering,
    IDelvisAutomatiskVilkårsvurdering by automatiskVilkårsvurdering,
    IDelvisManuellVilkårsvurdering by manuellVilkårsvurdering {

    override fun samletUtfall(): Utfall =
        manuellVilkårsvurdering.manuellVurdering()?.utfall ?: automatiskVilkårsvurdering.detIkkeManuelleUtfallet()

    override fun vurderinger(): List<Vurdering> =
        (automatiskVilkårsvurdering.vurderinger() + manuellVilkårsvurdering.manuellVurdering()).filterNotNull()

}
