package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.InstitusjonsoppholdsFaktum
import java.time.Period
import kotlin.reflect.KClass

object Institusjonsopphold : Vilkår {
    override val erInngangsVilkår: Boolean = true
    override val paragraf: Paragraf? = null
    override val relevanteFaktaTyper: List<KClass<out Faktum>> = listOf(InstitusjonsoppholdsFaktum::class)

    override fun vurder(faktum: List<Faktum>, vurderingsPeriode: Periode): Utfall {
        val instFakta = faktum as List<InstitusjonsoppholdsFaktum>
        val instFaktum = instFakta.first()

        return when {
            instFaktum.oppholdsperiode.inneholderHele(vurderingsPeriode) -> Utfall.VURDERT_OG_IKKE_OPPFYLT()
            instFaktum.oppholdsperiode.overlapperMed(vurderingsPeriode) -> Utfall.VURDERT_OG_OPPFYLT(
                vurderingsPeriode.intersect(instFaktum.oppholdsperiode)
            )
            else -> Utfall.VURDERT_OG_IKKE_OPPFYLT()
        }
    }
}
