package no.nav.tiltakspenger.vilkårsvurdering.vurdering

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.felles.Vilkårsvurdering

// TODO: Det er ikke avklart ennå at vi kan bruke Inst2 !
class InstitusjonsoppholdVilkårsvurdering(
    private val søknad: Søknad,
    // private val institusjonsopphold: List<Institusjonsopphold>?,
    private val vurderingsperiode: Periode
) : Vilkårsvurdering() {
    override fun vilkår(): Vilkår = Vilkår.INSTITUSJONSOPPHOLD

    private val søknadVurdering = lagVurderingFraSøknad()

    // private val inst2Vurderinger = lagVurderingerFraInst2()
    override var manuellVurdering: Vurdering? = null

    override fun detIkkeManuelleUtfallet(): Utfall {
        // val utfall = inst2Vurderinger.map { it.utfall } + søknadVurdering.utfall
        val utfall = listOf(søknadVurdering.utfall)
        return when {
            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.OPPFYLT
        }
    }

    private fun lagVurderingFraSøknad(): Vurdering = Vurdering(
        vilkår = vilkår(),
        kilde = SØKNADKILDE,
        fom = null,
        tom = null,
        utfall = if (søknad.oppholdInstitusjon == true) utfallFraTypeInstitusjon(søknad.typeInstitusjon) else Utfall.OPPFYLT,
        detaljer = "Opphold på ${søknad.typeInstitusjon}",
    )

    // Hentet fra https://github.com/navikt/soknadtiltakspenger/blob/1982b68dce426966f2f7d347028419c719cad9c6/app/js/informasjonsside/templates/sporsmalOmInstitusjon.html:
    // Mulig verdier er "barneverninstitusjon", "overgangsbolig" og "annet".
    // Regler hentet fra https://confluence.adeo.no/display/POAO/Avklaringsbehov+som+dukker+opp+mens+utviklerne+koder:
    // typeInstitusjon == null -> Manuell behandling
    // Overgangsbolig = oppfylt
    // Barneverninstitusjon = oppfylt
    // Annen type institusjon med fri kost og losji = Manuell behandling
    // TODO typeInstitusjon bør bli en enum
    private fun utfallFraTypeInstitusjon(typeInstitusjon: String?): Utfall =
        when (typeInstitusjon) {
            null -> Utfall.KREVER_MANUELL_VURDERING
            "overgangsbolig" -> Utfall.KREVER_MANUELL_VURDERING
            "barneverninstitusjon" -> Utfall.OPPFYLT
            "annet" -> Utfall.KREVER_MANUELL_VURDERING
            else -> Utfall.KREVER_MANUELL_VURDERING
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
    /*
    private fun lagVurderingerFraInst2(): List<Vurdering> =
        if (institusjonsopphold == null) {
            listOf(
                Vurdering(
                    vilkår = lovreferanse(),
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
                        vilkår = lovreferanse(),
                        kilde = INST2KILDE,
                        fom = it.startdato,
                        tom = it.faktiskSluttdato,
                        utfall = Utfall.IKKE_OPPFYLT,
                        detaljer = "",
                    )
                }.ifEmpty {
                    listOf(
                        Vurdering(
                            vilkår = lovreferanse(),
                            kilde = INST2KILDE,
                            fom = null,
                            tom = null,
                            utfall = Utfall.OPPFYLT,
                            detaljer = "",
                        )
                    )
                }
        }
     */

    override fun vurderinger(): List<Vurdering> = listOfNotNull(søknadVurdering, manuellVurdering)
    // (inst2Vurderinger + søknadVurdering + manuellVurdering).filterNotNull()

    companion object {
        private const val SØKNADKILDE = "SØKNAD"
        // private const val INST2KILDE = "Inst2"
    }
}
