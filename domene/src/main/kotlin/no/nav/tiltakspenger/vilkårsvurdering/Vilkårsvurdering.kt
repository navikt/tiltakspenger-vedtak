package no.nav.tiltakspenger.vilkårsvurdering

interface ILovreferanse {
    val lovreferanse: Lovreferanse
}

interface IVilkårsvurderingerKategori : ILovreferanse {
    fun samletUtfall(): Utfall
}

interface StatligYtelseVilkårsvurdering : IKomplettVilkårsvurdering, ILovreferanse, IDelvisManuellVilkårsvurdering

interface KommunalYtelseVilkårsvurdering : IKomplettVilkårsvurdering, ILovreferanse, IDelvisManuellVilkårsvurdering

sealed class Vilkårsvurdering : IKomplettVilkårsvurdering, ILovreferanse
