package no.nav.tiltakspenger.domene

import java.time.LocalDateTime

object FaktaInhenter {
    fun hentAldersFakta(saksbehandling: Saksbehandling) {
        Thread.sleep(2000)
        saksbehandling.opplys(AldersFaktum(
            ident = saksbehandling.ident,
            kilde = FaktumKilde.SYSTEM,
            alder = 17
        ))
    }
}

class Saksbehandling(
    private val startet: LocalDateTime = LocalDateTime.now(),
    val ident: String,
    vilkårsVurderinger: List<Vilkårsvurdering>
) {
    private var vilkårsVurderinger: List<Vilkårsvurdering> = vilkårsVurderinger
        get() = vilkårsVurderinger

    fun opplys(faktum: Faktum) {
        vilkårsVurderinger = vilkårsVurderinger.map { vilkår -> vilkår.vurder(faktum) }
    }

    fun erInngangOppfylt(): Boolean {
        return vilkårsVurderinger.erInngangsVilkårOppfylt()
    }

    companion object {
        fun start(ident: String) {
            val vurderinger = inngangsVilkår.map { Vilkårsvurdering(vilkår = it) }
            FaktaInhenter.hentAldersFakta(Saksbehandling(
                ident = ident,
                vilkårsVurderinger = vurderinger
            ))
        }
    }
}