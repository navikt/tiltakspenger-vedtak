package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

object PersonopplysningerBarnetilleggMapper {
    /*
    fun map(vurderingsperiode: Periode, personopplysninger: SakPersonopplysninger): List<BarnetilleggBarn> {
        return personopplysninger.barnMedIdent().map {
            BarnetilleggBarn(
                detaljer = KorrigerbareBarnDetaljer(
                    saksopplysning = BarnetilleggBarnPdl(
                        fornavn = it.fornavn,
                        mellomnavn = it.mellomnavn,
                        etternavn = it.etternavn,
                        fødselsdato = it.fødselsdato,
                        saksbehandler = TODO(),
                    ),
                ),
                harSøktBarnetilleggForDetteBarnet = KorrigerbartJaNeiVilkår(
                    vilkår = Vilkår.AAP, // TODO
                    opprinneligSaksopplysning = JaNeiSaksopplysning(
                        kilde = Kilde.PDL,
                        detaljer = "",
                        saksbehandler = TODO(),
                        verdi = false,
                    ),
                ),
                forsørgesAvSøker = KorrigerbartJaNeiPeriodeVilkår(
                    vilkår = Vilkår.AAP, // TODO
                    vurderingsperiode = vurderingsperiode,
                    saksopplysning = JaNeiPeriodeSaksopplysning(
                        kilde = Kilde.PDL,
                        detaljer = "",
                        saksbehandler = TODO(),
                        verdi = Periodisering(periodeMedVerdi = mapOf(vurderingsperiode to true)),
                    ),
                ),
                bosattIEØS = KorrigerbartJaNeiPeriodeVilkår(
                    vilkår = Vilkår.AAP, // TODO
                    vurderingsperiode = vurderingsperiode,
                    saksopplysning = JaNeiPeriodeSaksopplysning(
                        kilde = Kilde.PDL,
                        detaljer = "",
                        saksbehandler = TODO(),
                        verdi = Periodisering(periodeMedVerdi = mapOf(vurderingsperiode to true)),
                    ),
                ),
                oppholderSegIEØS = KorrigerbartJaNeiPeriodeVilkår(
                    vilkår = Vilkår.AAP, // TODO
                    vurderingsperiode = vurderingsperiode,
                    saksopplysning = JaNeiPeriodeSaksopplysning(
                        kilde = Kilde.PDL,
                        detaljer = "",
                        saksbehandler = TODO(),
                        verdi = Periodisering(periodeMedVerdi = mapOf(vurderingsperiode to true)),
                    ),
                ),
            )
        }
        // TODO + personopplysninger.barnUtenIdent().map { }
    }

     */
}
