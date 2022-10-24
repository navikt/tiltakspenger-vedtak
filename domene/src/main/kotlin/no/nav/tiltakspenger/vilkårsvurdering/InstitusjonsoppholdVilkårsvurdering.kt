package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Institusjonsopphold
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate

class InstitusjonsoppholdVilkårsvurdering(
    private val søknad: Søknad,
    private val institusjonsopphold: List<Institusjonsopphold>?,
    private val vurderingsperiode: Periode
) : Vilkårsvurdering() {
    override fun lovreferanse(): Lovreferanse = Lovreferanse.INSTITUSJONSOPPHOLD

    private val søknadVurdering = lagVurderingFraSøknad()
    private val inst2Vurderinger = lagVurderingerFraInst2()
    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet(): Utfall {
        val utfall = inst2Vurderinger.map { it.utfall } + søknadVurdering.utfall
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    /*
    Institusjonstype:
    Hvilken type institusjon oppholdet var registrert på.
    Kan være kodeverdiene AS (Alders- og sykehjem), FO (Fengsel) eller HS (Helseinsitusjon)
    Kategori:
    Kodeverdi for oppholdskategori. Kan være kodeverdiene A (Alders- og sykehjem), D (Dagpasient), F (Ferieopphold),
    H (Heldøgnpasient), P (Fødsel), R (Opptreningsinstitusjon), S (Soningsfange) eller V (Varetektsfange)
     */
    // TODO: Logikken her må kvalitetssikres
    private fun lagVurderingerFraInst2(): List<Vurdering> =
        if (institusjonsopphold == null) {
            listOf(
                Vurdering(
                    lovreferanse = lovreferanse(),
                    kilde = INST2KILDE,
                    fom = null,
                    tom = null,
                    utfall = Utfall.IKKE_IMPLEMENTERT,
                    detaljer = "",
                )
            )
        } else {
            institusjonsopphold
                .filter { Periode(it.startdato, it.faktiskSluttdato ?: LocalDate.MAX).overlapperMed(vurderingsperiode) }
                .filter { it.institusjonstype == "FO" } // TODO: Hva hvis den er null?
                .filter { it.kategori in listOf("R", "S") } // TODO: Hva hvis den er null?
                .map {
                    Vurdering(
                        lovreferanse = lovreferanse(),
                        kilde = INST2KILDE,
                        fom = it.startdato,
                        tom = it.faktiskSluttdato,
                        utfall = Utfall.IKKE_OPPFYLT,
                        detaljer = "",
                    )
                }.ifEmpty {
                    listOf(
                        Vurdering(
                            lovreferanse = lovreferanse(),
                            kilde = INST2KILDE,
                            fom = null,
                            tom = null,
                            utfall = Utfall.OPPFYLT,
                            detaljer = "",
                        )
                    )
                }
        }


    private fun lagVurderingFraSøknad(): Vurdering = Vurdering(
        lovreferanse = lovreferanse(),
        kilde = SØKNADKILDE,
        fom = null,
        tom = null,
        utfall = if (søknad.deltarKvp) Utfall.KREVER_MANUELL_VURDERING else Utfall.OPPFYLT,
        detaljer = "",
    )

    override fun vurderinger(): List<Vurdering> =
        (inst2Vurderinger + søknadVurdering + manuellVurdering).filterNotNull()

    companion object {
        private const val SØKNADKILDE = "SØKNAD"
        private const val INST2KILDE = "Inst2"
    }
}
