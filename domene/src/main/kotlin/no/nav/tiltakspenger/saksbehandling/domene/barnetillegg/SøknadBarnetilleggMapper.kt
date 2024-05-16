package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Barnetillegg
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

object SøknadBarnetilleggMapper {

    fun map(søknad: Søknad, vurderingsperiode: Periode, systembruker: Systembruker): List<BarnetilleggBarn> {
        return søknad.barnetillegg.map {
            BarnetilleggBarn(
                detaljer = KorrigerbareBarnDetaljer(
                    saksopplysning = when (it) {
                        is Barnetillegg.FraPdl ->
                            BarnetilleggBarnPdl(
                                fornavn = it.fornavn ?: "?",
                                mellomnavn = it.mellomnavn ?: "",
                                etternavn = it.etternavn ?: "?",
                                fødselsdato = it.fødselsdato,
                                saksbehandler = systembruker.brukernavn,
                            )

                        is Barnetillegg.Manuell ->
                            BarnetilleggBarnManuell(
                                fornavn = it.fornavn,
                                mellomnavn = it.mellomnavn ?: "",
                                etternavn = it.etternavn,
                                fødselsdato = it.fødselsdato,
                                saksbehandler = systembruker.brukernavn,
                            )
                    },
                ),
                harSøktBarnetilleggForDetteBarnet = KorrigerbartJaNeiVilkår(
                    vilkår = Vilkår.AAP,
                    opprinneligSaksopplysning = JaNeiSaksopplysning(
                        kilde = Kilde.SØKNAD,
                        detaljer = "",
                        saksbehandler = systembruker.brukernavn,
                        verdi = JaNei.JA,
                    ),
                ),
                forsørgesAvSøker = KorrigerbartJaNeiPeriodeVilkår(
                    vilkår = Vilkår.AAP,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysning = JaNeiPeriodeSaksopplysning(
                        kilde = Kilde.SØKNAD,
                        detaljer = "",
                        saksbehandler = systembruker.brukernavn,
                        verdi = Periodisering(periodeMedVerdi = mapOf(vurderingsperiode to JaNei.JA)),
                    ),
                ),
                bosattIEØS = KorrigerbartJaNeiPeriodeVilkår(
                    vilkår = Vilkår.AAP,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysning = JaNeiPeriodeSaksopplysning(
                        kilde = Kilde.SØKNAD,
                        detaljer = "",
                        saksbehandler = systembruker.brukernavn,
                        verdi = Periodisering(periodeMedVerdi = mapOf(vurderingsperiode to JaNei.JA)),
                    ),
                ),
                oppholderSegIEØS = KorrigerbartJaNeiPeriodeVilkår(
                    vilkår = Vilkår.AAP,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysning = JaNeiPeriodeSaksopplysning(
                        kilde = Kilde.SØKNAD,
                        detaljer = "",
                        saksbehandler = systembruker.brukernavn,
                        verdi = Periodisering(periodeMedVerdi = mapOf(vurderingsperiode to JaNei.JA)),
                    ),
                ),
            )
        }
    }
}
