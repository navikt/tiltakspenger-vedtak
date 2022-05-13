package no.nav.tiltakspenger.domene.vilkår

import no.nav.tiltakspenger.domene.*
import no.nav.tiltakspenger.domene.fakta.InstitusjonsoppholdsFaktum
import java.time.Period
import kotlin.reflect.KClass

object Institusjonsopphold : Vilkår {
    override val erInngangsVilkår: Boolean
        get() = TODO("Not yet implemented")
    override val paragraf: Paragraf?
        get() = TODO("Not yet implemented")
    override val relevanteFaktaTyper: List<KClass<out Faktum>>
        get() = TODO("Not yet implemented")

    override fun vurder(faktum: List<Faktum>, vurderingsPeriode: Periode): Utfall {
        val instFaktum = faktum as InstitusjonsoppholdsFaktum
        return when {
            instFaktum.oppholdsperiode.inneholderHele(vurderingsPeriode) -> Utfall.VURDERT_OG_IKKE_OPPFYLT()
            instFaktum.oppholdsperiode.overlapperMed(vurderingsPeriode) -> Utfall.VURDERT_OG_OPPFYLT(
                Periode(
                    fra = maxOf(instFaktum.oppholdsperiode.fra, vurderingsPeriode.fra),
                    til = minOf(instFaktum.oppholdsperiode.til, vurderingsPeriode.til)
                )
            )
            else -> Utfall.VURDERT_OG_IKKE_OPPFYLT()
        }
    }
}
