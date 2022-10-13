package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

class KVPVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) {
    val søknadVurdering = Vurdering(
        kilde = "Søknad",
        fom = null,
        tom = null,
        utfall = avgjørUtfall()
    )

    private fun avgjørUtfall(): Utfall {
        return if (søknad.deltarKvp) return Utfall.KREVER_MANUELL_VURDERING else Utfall.OPPFYLT
    }

    var manuellVurdering: Vurdering? = null

    fun vurderinger(): List<Vurdering> = listOfNotNull(søknadVurdering, manuellVurdering)

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall) {
        manuellVurdering = Vurdering(kilde = "Saksbehandler", fom = fom, tom = tom, utfall = utfall)
    }

    fun samletUtfall(): Utfall {
        return manuellVurdering?.utfall ?: søknadVurdering.utfall
    }

}
