package no.nav.tiltakspenger.vilkårsvurdering

import java.time.LocalDate

interface IVilkårsvurderingerKategori : ILovreferanse {
    fun samletUtfall(): Utfall
}

sealed class Vilkårsvurdering : IVilkårsvurdering, ILovreferanse

interface ILovreferanse {
    val lovreferanse: Lovreferanse
}

interface IVilkårsvurdering {

    fun samletUtfall(): Utfall

    fun vurderinger(): List<Vurdering>
}

interface StatligYtelseVilkårsvurdering : IVilkårsvurdering, ILovreferanse, IManuellVilkårsvurdering

interface KommunalYtelseVilkårsvurdering : IVilkårsvurdering, ILovreferanse, IManuellVilkårsvurdering

interface IAutomatiskVilkårsvurdering {
    fun vurderinger(): List<Vurdering>

    fun detIkkeManuelleUtfallet(): Utfall
}


interface IManuellVilkårsvurdering {

    fun manuellVurdering(): Vurdering?

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String)
}

class BaseManuellVilkårsvurdering : IManuellVilkårsvurdering {
    private var manuellVurdering: Vurdering? = null
    override fun manuellVurdering(): Vurdering? = manuellVurdering

    override fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String) {
        manuellVurdering = Vurdering(
            kilde = "Saksbehandler",
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer
        )
    }
}

class KunManuellVilkårsvurderinger(
    private val manuellVilkårsvurdering: BaseManuellVilkårsvurdering
) : IVilkårsvurdering,
    IManuellVilkårsvurdering by manuellVilkårsvurdering {

    override fun samletUtfall(): Utfall = manuellVurdering()?.utfall ?: Utfall.KREVER_MANUELL_VURDERING // ?

    override fun vurderinger(): List<Vurdering> = listOfNotNull(manuellVurdering())
}

class BaseManuellOgAutomatiskVilkårsvurdering(
    private val manuellVilkårsvurdering: IManuellVilkårsvurdering = BaseManuellVilkårsvurdering(),
    private val automatiskVilkårsvurdering: IAutomatiskVilkårsvurdering,
) : IVilkårsvurdering,
    IAutomatiskVilkårsvurdering by automatiskVilkårsvurdering,
    IManuellVilkårsvurdering by manuellVilkårsvurdering {

    override fun samletUtfall(): Utfall =
        manuellVilkårsvurdering.manuellVurdering()?.utfall ?: automatiskVilkårsvurdering.detIkkeManuelleUtfallet()

    override fun vurderinger(): List<Vurdering> =
        (automatiskVilkårsvurdering.vurderinger() + manuellVilkårsvurdering.manuellVurdering()).filterNotNull()

}
