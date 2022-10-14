package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

class IntroProgrammetVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) {
    private val søknadVurdering = lagSøknadvurdering()

    private var manuellVurdering: Vurdering? = null

    private fun lagSøknadvurdering() = Vurdering(
        kilde = "Søknad",
        fom = søknad.introduksjonsprogrammetDetaljer?.fom,
        tom = søknad.introduksjonsprogrammetDetaljer?.tom,
        utfall = avgjørUtfall()
    )

    private fun avgjørUtfall(): Utfall {
        if (!søknad.deltarIntroduksjonsprogrammet) return Utfall.OPPFYLT
        val tom = søknad.introduksjonsprogrammetDetaljer?.tom ?: LocalDate.MAX
        return if (vurderingsperiode.overlapperMed(Periode(søknad.introduksjonsprogrammetDetaljer!!.fom, tom))) {
            Utfall.IKKE_OPPFYLT
        } else {
            Utfall.OPPFYLT
        }
    }

    fun vurderinger(): List<Vurdering> = listOfNotNull(søknadVurdering, manuellVurdering)

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall) {
        manuellVurdering = Vurdering(kilde = "Saksbehandler", fom = fom, tom = tom, utfall = utfall)
    }

    fun samletUtfall() = manuellVurdering?.utfall ?: søknadVurdering.utfall
}
