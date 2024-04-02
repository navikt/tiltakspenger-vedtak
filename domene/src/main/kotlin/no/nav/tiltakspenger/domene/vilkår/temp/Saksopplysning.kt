package no.nav.tiltakspenger.domene.vilkår.temp

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vilkår

sealed interface Saksopplysning : Sammenlignbar<Saksopplysning> {
    val vilkår: Vilkår
    val kilde: Kilde
    val typeSaksopplysning: TypeSaksopplysning

    override fun sammenlignbareFelter(): Set<*> = setOf(this.vilkår, this.kilde, this.typeSaksopplysning)
}

open class SaksopplysningFraKilde(
    override val vilkår: Vilkår,
    override val kilde: Kilde,
    override val typeSaksopplysning: TypeSaksopplysning,
) : Saksopplysning

class SaksopplysningFraSaksbehandler(
    vilkår: Vilkår,
    typeSaksopplysning: TypeSaksopplysning,
    val saksbehandler: String? = null,
) : SaksopplysningFraKilde(vilkår, Kilde.SAKSB, typeSaksopplysning)

class TomSaksopplysning(
    vilkår: Vilkår,
    kilde: Kilde,
) : SaksopplysningFraKilde(
    vilkår = vilkår,
    kilde = kilde,
    typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
)
