package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.InstitusjonsoppholdsFaktum
import kotlin.reflect.KClass

object Institusjonsopphold : Vilkår {
    override val erInngangsVilkår: Boolean = true
    override val paragraf: Paragraf? = null
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(InstitusjonsoppholdsFaktum::class)

    override fun vurder(faktum: List<Faktum>, vurderingsPeriode: Periode): Utfall {
        val instFakta = faktum as List<InstitusjonsoppholdsFaktum>
        val instFaktum = instFakta.first()

        return when {
            instFaktum.oppholdsperiode.inneholderHele(vurderingsPeriode) -> Utfall.VurdertOgIkkeOppfylt()
            instFaktum.oppholdsperiode.overlapperMed(vurderingsPeriode) -> Utfall.VurdertOgOppfylt(
                vurderingsPeriode.overlappendePeriode(instFaktum.oppholdsperiode)
            )
            else -> Utfall.VurdertOgIkkeOppfylt()
        }
    }
}
