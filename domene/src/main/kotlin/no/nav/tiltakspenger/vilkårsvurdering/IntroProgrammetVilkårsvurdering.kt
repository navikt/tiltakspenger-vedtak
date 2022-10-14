package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

enum class Lovreferanse(val lovverk: String, val paragraf: String, val ledd: String?, val beskrivelse: String) {
    INTROPROGRAMMET("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KVP("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KOMMUNALE_YTELSER("Tiltakspengeforskriften", "§7", null, "Forholdet til andre ytelser"),
}

class IntroProgrammetVilkårsvurdering(
    private val søknad: Søknad,
    private val vurderingsperiode: Periode
) {
    val lovReferanse: Lovreferanse = Lovreferanse.INTROPROGRAMMET
    private val søknadVurdering = lagSøknadvurdering()

    private var manuellVurdering: Vurdering? = null

    private fun lagSøknadvurdering() = Vurdering(
        kilde = "Søknad",
        fom = søknad.introduksjonsprogrammetDetaljer?.fom,
        tom = søknad.introduksjonsprogrammetDetaljer?.tom,
        utfall = avgjørUtfall(),
        detaljer = "",
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

    fun settManuellVurdering(fom: LocalDate, tom: LocalDate, utfall: Utfall, detaljer: String) {
        manuellVurdering = Vurdering(
            kilde = "Saksbehandler",
            fom = fom,
            tom = tom,
            utfall = utfall,
            detaljer = detaljer
        )
    }

    fun samletUtfall() = manuellVurdering?.utfall ?: søknadVurdering.utfall
}
