package no.nav.tiltakspenger.domene.alternativ

interface VilkårsvurderingVisitor {

    fun visit(over18Vilkårsvurdering: Over18Vilkårsvurdering)
}

class FaktaFyllerVisitor(private val faktum: Faktum) : VilkårsvurderingVisitor {
    override fun visit(vv: Over18Vilkårsvurdering) {
        if (faktum is FødselsdatoFaktum) {
            vv.oppdaterFaktum(faktum)
        }
        //eller
        vv.fyllInnFaktumDerDetPasser(faktum)
    }
}

class IkkeVurderteVilkårVisitor : VilkårsvurderingVisitor {
    private val vilkår = mutableListOf<Vilkår>()

    override fun visit(over18Vilkårsvurdering: Over18Vilkårsvurdering) {
        if (over18Vilkårsvurdering.faktum == null) {
            vilkår.add(Over18Vilkår)
        }
    }

}