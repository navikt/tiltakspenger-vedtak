package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.*
import java.time.Period
import kotlin.reflect.KClass

object Institusjonsopphold : Vilkår {
    override val erInngangsVilkår: Boolean
        get() = TODO("Not yet implemented")
    override val paragraf: Paragraf?
        get() = TODO("Not yet implemented")
    override val relevanteFaktaTyper: List<KClass<out Faktum>>
        get() = TODO("Not yet implemented")

    override fun vurder(faktum: List<Faktum>): Utfall {
        TODO("Not yet implemented")
    }

}
