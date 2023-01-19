package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering
import java.time.LocalDate

class AlderVilkårsvurdering(vurderingsperiode: Periode, søkersFødselsdato: LocalDate) : Vilkårsvurdering() {

    fun splittVurderingsperiodePåDato(
        vurderingsperiode: Periode,
        datoBrukerFyller18År: LocalDate
    ): Pair<Periode, Periode> {
        val periodeFørBrukerFyller18År = Periode(
            fra = vurderingsperiode.fra,
            til = datoBrukerFyller18År.minusDays(1)
        )
        val periodeEtterBrukerFyller18År = Periode(
            fra = datoBrukerFyller18År,
            til = vurderingsperiode.til
        )
        return Pair(periodeFørBrukerFyller18År, periodeEtterBrukerFyller18År)
    }

    fun brukerEr18ÅrIHeleVurderingsperioden(vurderingsperiode: Periode, datoSøkerFyller18År: LocalDate): Boolean =
        vurderingsperiode.etter(datoSøkerFyller18År) || vurderingsperiode.fra.isEqual(datoSøkerFyller18År)

    fun brukerFyller18ÅrEtterVurderingsperioden(vurderingsperiode: Periode, datoSøkerFyller18År: LocalDate): Boolean =
        vurderingsperiode.før(datoSøkerFyller18År)


    fun lagAlderVurderinger(vurderingsperiode: Periode, søkersFødselsdato: LocalDate): List<Vurdering> {
        val datoBrukerFyller18År = søkersFødselsdato.plusYears(18)
        if (brukerEr18ÅrIHeleVurderingsperioden(vurderingsperiode, datoBrukerFyller18År)) {
            return listOf(
                lagOppfyltVurdering(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til
                )
            )
        }
        if (brukerFyller18ÅrEtterVurderingsperioden(vurderingsperiode, datoBrukerFyller18År)) {
            return listOf(
                lagIkkeOppfyltVurdering(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til,
                    null
                )
            )
        }
        val (periodeUnder18År, periodeFylt18År) = splittVurderingsperiodePåDato(
            vurderingsperiode = vurderingsperiode,
            datoBrukerFyller18År = datoBrukerFyller18År
        )
        return listOf(
            lagIkkeOppfyltVurdering(
                fra = periodeUnder18År.fra,
                til = periodeUnder18År.til,
                detaljer = "Bruker fyller 18 år i søknadsperioden"
            )
        )
    }


    fun lagOppfyltVurdering(fra: LocalDate, til: LocalDate): Vurdering =
        Vurdering(
            vilkår = vilkår(),
            kilde = "PDL",
            fom = fra,
            tom = til,
            utfall = Utfall.OPPFYLT,
            detaljer = "-"
        )

    fun lagIkkeOppfyltVurdering(fra: LocalDate, til: LocalDate, detaljer: String?): Vurdering =
        Vurdering(
            vilkår = vilkår(),
            kilde = "PDL",
            fom = fra,
            tom = til,
            utfall = Utfall.IKKE_OPPFYLT,
            detaljer = if (detaljer.isNullOrEmpty()) "-" else detaljer,
        )

    val alderVurderinger: List<Vurdering> = lagAlderVurderinger(
        vurderingsperiode = vurderingsperiode,
        søkersFødselsdato = søkersFødselsdato
    )

    override fun vurderinger(): List<Vurdering> {
        return alderVurderinger;
    }

    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = alderVurderinger.map { it.utfall }
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            else -> Utfall.OPPFYLT
        }
    }

    override var manuellVurdering: Vurdering? = null
    override fun vilkår(): Vilkår = Vilkår.ALDER
}